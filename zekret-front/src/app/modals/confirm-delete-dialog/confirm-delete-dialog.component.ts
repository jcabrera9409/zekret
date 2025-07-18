import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { ConfirmDeleteDataDTO } from '../../_model/dto';
import { FormsModule } from '@angular/forms';


@Component({
  selector: 'app-confirm-delete-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, FormsModule],
  templateUrl: './confirm-delete-dialog.component.html',
  styleUrl: './confirm-delete-dialog.component.css'
})
export class ConfirmDeleteDialogComponent {

  confirmText: string = '';

  constructor(
    public dialogRef: MatDialogRef<ConfirmDeleteDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ConfirmDeleteDataDTO
  ) {
    // Valores por defecto
    this.data = {
      title: data?.title || 'Confirmar eliminación',
      message: data?.message || '¿Estás seguro de que deseas eliminar este elemento?',
      confirmText: data?.confirmText || 'Eliminar',
      cancelText: data?.cancelText || 'Cancelar'
    };
  }

  onConfirm(): void {
    if (this.confirmText.toLowerCase() !== 'confirm') {
      return;
    }
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
