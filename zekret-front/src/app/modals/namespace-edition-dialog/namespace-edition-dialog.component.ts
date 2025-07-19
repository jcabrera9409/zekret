import { Component, Inject } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Namespace } from '../../_model/namespace';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { NamespaceService } from '../../_service/namespace.service';
import { catchError, EMPTY, switchMap } from 'rxjs';
import { Message } from '../../_model/message';
import { LoaderComponent } from "../../shared/loader/loader.component";
import { NotificationService } from '../../_service/notification.service';

@Component({
  selector: 'app-namespace-edition-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LoaderComponent],
  templateUrl: './namespace-edition-dialog.component.html',
  styleUrl: './namespace-edition-dialog.component.css'
})
export class NamespaceEditionDialogComponent {

  form: FormGroup;
  tituloDialog: string = 'Crear Namespace';
  isLoading: boolean = false;

  constructor(
    private dialogRef: MatDialogRef<NamespaceEditionDialogComponent>,
    private namespaceService: NamespaceService,
    private notificationService: NotificationService,
    @Inject(MAT_DIALOG_DATA) private namespace: Namespace | null
  ) {
    this.form = new FormGroup({
      name: new FormControl(this.namespace ? this.namespace.name : '', [Validators.required, Validators.maxLength(50)]),
      description: new FormControl(this.namespace ? this.namespace.description : '', [Validators.required, Validators.maxLength(200)])
    });
    if (this.namespace) {
      this.tituloDialog = 'Editar Namespace';
    }
  }

  onSubmit() {
    if (this.form.invalid) {
      this.notificationService.setMessageChange(
        Message.error('Por favor, completa todos los campos requeridos')
      );
      return;
    }

    this.isLoading = true;
    let namespaceData: Namespace = new Namespace();
    namespaceData.zrn = this.namespace ? this.namespace.zrn : '';
    namespaceData.name = this.form.value.name;
    namespaceData.description = this.form.value.description;

    const operation = (this.namespace)
      ? this.namespaceService.modifyByZrn(namespaceData.zrn, namespaceData)
      : this.namespaceService.register(namespaceData);

    operation
      .pipe(
        catchError(error => {
          this.notificationService.setMessageChange(
            Message.error('Ocurrió un error al procesar la operación', error)
          );
          this.isLoading = false;
          return EMPTY;
        }),
        switchMap(() => this.namespaceService.getAll()),
        catchError(error => {
          this.notificationService.setMessageChange(
            Message.error('Ocurrió un error al obtener los namespaces', error)
          );
          this.isLoading = false;
          return EMPTY;
        })
      )
      .subscribe({
        next: (response) => {
          if (response.success) {
            this.namespaceService.setChangeObject(response.data);
            this.notificationService.setMessageChange(
              Message.success('Namespace procesado correctamente')
            );
            this.dialogRef.close();
          } else {
            this.notificationService.setMessageChange(
              Message.error('Error al procesar el namespace', response.message)
            );
          }
          this.isLoading = false;
        },
        error: (error) => {
          this.notificationService.setMessageChange(
            Message.error('Ocurrió un error al procesar el namespace', error)
          );
          this.isLoading = false;
        }
      });

  }

  onCancel() {
    this.dialogRef.close();
  }
}
