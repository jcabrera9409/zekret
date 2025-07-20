import { Component, Inject, Renderer2 } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { credentialType } from '../../_model/credential-type';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Namespace } from '../../_model/namespace';
import { Credential } from '../../_model/credential';
import { CredentialService } from '../../_service/credential.service';
import { catchError, EMPTY, finalize, switchMap } from 'rxjs';
import { Message } from '../../_model/message';
import { LoaderComponent } from "../../shared/loader/loader.component";
import { NotificationService } from '../../_service/notification.service';
import { FormMethods } from '../../util/forms';

@Component({
  selector: 'app-credential-edition-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LoaderComponent],
  templateUrl: './credential-edition-dialog.component.html',
  styleUrl: './credential-edition-dialog.component.css'
})

export class CredentialEditionDialogComponent {

  isLoading: boolean = false;
  dialogTitle: string = 'Crear Credencial';
  
  credentialType = credentialType;

  credentialForm: FormGroup;

  constructor(
    private dialogRef: MatDialogRef<CredentialEditionDialogComponent>,
    private credentialService: CredentialService,
    private notificationService: NotificationService,
    private renderer: Renderer2,
    @Inject(MAT_DIALOG_DATA) public data: CredentialEditionDialogData
  ) {
    this.credentialForm = new FormGroup({
      title: new FormControl( this.data.credential ? this.data.credential.title : '' , [Validators.required]),
      type: new FormControl( this.data.credential ? this.data.credential.credentialType.zrn : "username_password", [Validators.required]),
      
      up_username: new FormControl(this.data.credential ? this.data.credential.username : '', [Validators.required]),
      up_password: new FormControl(this.data.credential ? this.data.credential.password : '', [Validators.required]),

      ssh_public_key: new FormControl(this.data.credential ? this.data.credential.sshPublicKey : ''),
      ssh_private_key: new FormControl(this.data.credential ? this.data.credential.sshPrivateKey : ''),

      secret_text: new FormControl(this.data.credential ? this.data.credential.secretText : ''),

      file_name: new FormControl(this.data.credential ? this.data.credential.fileName : ''),
      file_content: new FormControl(this.data.credential ? this.data.credential.fileContent : ''),

      notas: new FormControl(this.data.credential ? this.data.credential.notes : ''),
      
    });

    if(this.data.credential) {
      this.dialogTitle = 'Editar Credencial';
      this.changeValidators(this.data.credential.credentialType.zrn);
    }

    FormMethods.addSubscribesForm(this.credentialForm, renderer);
  }

  onTypeChange(event: any) {
    const selectedType = event.target.value;
    this.changeValidators(selectedType);
  }

  changeValidators(credentialType: string) {
    for (const controlName in this.credentialType) {
      for (const input of this.credentialType[controlName].inputs) {
        this.credentialForm.get(input.formControlName)?.clearValidators();
        this.credentialForm.get(input.formControlName)?.updateValueAndValidity();
      }
    }

    const inputs = this.credentialType[credentialType]?.inputs || [];
    inputs.forEach(input => {
      const controlName = input.formControlName;
      this.credentialForm.get(controlName)?.setValidators(input.validator);
      this.credentialForm.get(controlName)?.updateValueAndValidity();
    });
  }

  isTypeSelected(type: string): boolean {
    return this.credentialForm.get('type')?.value === type;
  }

  onSubmit() {
    if (this.credentialForm.invalid) {
      FormMethods.validateForm(this.credentialForm, this.renderer);
      this.notificationService.setMessageChange(
        Message.error('Por favor complete todos los campos requeridos')
      );
      return;
    }

    this.isLoading = true;
    let credentialData: Credential = new Credential();
    credentialData.zrn = this.data.credential ? this.data.credential.zrn : '';
    credentialData.title = this.credentialForm.value.title;
    credentialData.credentialType = this.credentialType[this.credentialForm.value.type] //this.credentialType.find(ct => ct.zrn === this.credentialForm.value.type);
    credentialData.namespace = this.data.namespace;
    credentialData.notes = this.credentialForm.value.notas;
    
    switch (credentialData.credentialType.zrn) {
      case 'username_password':
        credentialData.username = this.credentialForm.value.up_username;
        credentialData.password = this.credentialForm.value.up_password;
        break;
      case 'ssh_username':
        credentialData.username = this.credentialForm.value.up_username;
        credentialData.sshPublicKey = this.credentialForm.value.ssh_public_key;
        credentialData.sshPrivateKey = this.credentialForm.value.ssh_private_key;
        break;
      case 'secret_text':
        credentialData.secretText = this.credentialForm.value.secret_text;
        break;
      case 'file':
        credentialData.fileName = this.credentialForm.value.file_name;
        credentialData.fileContent = this.credentialForm.value.file_content;
        break;
    }

    const operation = (this.data.credential)
      ? this.credentialService.modifyByZrn(credentialData.zrn, credentialData)
      : this.credentialService.register(credentialData);

    operation
      .pipe(
        catchError(error => {
          this.notificationService.setMessageChange(
            Message.error('Ocurrió un error al procesar la operación', error)
          );
          return EMPTY;
        }),
        switchMap(() => this.credentialService.getAllByNamespaceZrn(this.data.namespace.zrn)),
        catchError(error => {
          this.notificationService.setMessageChange(
            Message.error('Ocurrió un error al obtener las credenciales', error)
          );
          return EMPTY;
        }),
        finalize(() => {
          this.isLoading = false
        })
      )
      .subscribe((response) => {
          if (response.success) {
            this.credentialService.setChangeObject(response.data);
            this.notificationService.setMessageChange(
              Message.success('Credencial procesada correctamente')
            );
            this.dialogRef.close();
          } else {
            this.notificationService.setMessageChange(
              Message.error('Error al procesar la credencial', response.message)
            );
          }
      });
  }

  closeDialog() {
    this.dialogRef.close();
  }
}

export interface CredentialEditionDialogData {
  namespace: Namespace; 
  credential: Credential; 
}