import { Component, Inject } from '@angular/core';
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
    @Inject(MAT_DIALOG_DATA) public data: CredentialEditionDialogData
  ) {
    this.credentialForm = new FormGroup({
      title: new FormControl( this.data.credential ? this.data.credential.title : '' , [Validators.required]),
      type: new FormControl( this.data.credential ? this.data.credential.credentialType.zrn : credentialType[0].zrn, [Validators.required]),
      
      up_username: new FormControl(this.data.credential ? this.data.credential.username : ''),
      up_password: new FormControl(this.data.credential ? this.data.credential.password : ''),

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
  }

  onTypeChange(event: any) {
    const selectedType = event.target.value;
    this.changeValidators(selectedType);
  }

  changeValidators(credentialType: string) {
    this.credentialForm.get('up_username')?.clearValidators();
    this.credentialForm.get('up_password')?.clearValidators();
    this.credentialForm.get('ssh_private_key')?.clearValidators();
    this.credentialForm.get('secret_text')?.clearValidators();
    this.credentialForm.get('file_name')?.clearValidators();
    this.credentialForm.get('file_content')?.clearValidators();
    switch (credentialType) {
      case 'username_password':
        this.credentialForm.get('up_username')?.setValidators([Validators.required]);
        this.credentialForm.get('up_password')?.setValidators([Validators.required]);
        break;
      case 'ssh_username':
        this.credentialForm.get('up_username')?.setValidators([Validators.required]);
        this.credentialForm.get('ssh_private_key')?.setValidators([Validators.required]);
        break;
      case 'secret_text':
        this.credentialForm.get('secret_text')?.setValidators([Validators.required]);
        break;
      case 'file':
        this.credentialForm.get('file_name')?.setValidators([Validators.required]);
        this.credentialForm.get('file_content')?.setValidators([Validators.required]);
        break;
    }
  }

  isTypeSelected(type: string): boolean {
    return this.credentialForm.get('type')?.value === type;
  }

  onSubmit() {
    if (this.credentialForm.invalid) {
      this.notificationService.setMessageChange(
        Message.error('Por favor complete todos los campos requeridos')
      );
      return;
    }

    this.isLoading = true;
    let credentialData: Credential = new Credential();
    credentialData.zrn = this.data.credential ? this.data.credential.zrn : '';
    credentialData.title = this.credentialForm.value.title;
    credentialData.credentialType = this.credentialType.find(ct => ct.zrn === this.credentialForm.value.type);
    credentialData.namespace = this.data.namespace;
    credentialData.notes = this.credentialForm.value.notas;
    
    switch (credentialData.credentialType.zrn) {
      case 'username_password':
        credentialData.username = this.credentialForm.value.up_username;
        credentialData.password = this.credentialForm.value.up_password;
        break;
      case 'ssh_username':
        credentialData.username = this.credentialForm.value.up_username;
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