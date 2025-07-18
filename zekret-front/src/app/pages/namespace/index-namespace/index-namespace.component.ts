import { Component, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { NamespaceEditionDialogComponent } from '../../../modals/namespace-edition-dialog/namespace-edition-dialog.component';
import { ConfirmDeleteDialogComponent } from '../../../modals/confirm-delete-dialog/confirm-delete-dialog.component';
import { NamespaceService } from '../../../_service/namespace.service';
import { Namespace } from '../../../_model/namespace';
import { ConfirmDeleteDataDTO } from '../../../_model/dto';


@Component({
  selector: 'app-index-namespace',
  standalone: true,
  imports: [CommonModule, MatDialogModule],
  templateUrl: './index-namespace.component.html',
  styleUrl: './index-namespace.component.css'
})
export class IndexNamespaceComponent implements OnInit {
  @Output() namespaceSelected = new EventEmitter<Namespace>();

  namespaces: Namespace[] = [];

  constructor(
    private namespaceService: NamespaceService,
    private dialog: MatDialog
  ){}

  ngOnInit(): void {
    this.getAllNamespaces();
    this.namespaceService.getChangeObject().subscribe({
      next: (namespaces) => {
        this.namespaces = namespaces;
        console.log('Namespaces updated:', this.namespaces);
      }
      , error: (error) => {
        console.error('Error updating namespaces:', error);
      }
    });
  }

  getAllNamespaces() {
    this.namespaceService.getAll().subscribe({
      next: (response) => {
        if (response.success) {
          this.namespaceService.setChangeObject(response.data);
          console.log('Namespaces loaded successfully:', response.data);
        } else {
          this.namespaceService.setChangeObject([]);
          console.error('Failed to load namespaces:', response.message);
        }
      }
      , error: (error) => {
        this.namespaceService.setChangeObject([]);
        console.error('Error fetching namespaces:', error);
      }
    });
  }

  onNamespaceClick(namespace: Namespace) {
    this.namespaceSelected.emit(namespace);
  }

  openCreateNamespaceDialog(namespace: Namespace = null) {
    this.dialog.open(NamespaceEditionDialogComponent, {
      width: '500px',
      data: namespace
    });
  }

  deleteNamespace(namespace: Namespace) {
    const dialogData: ConfirmDeleteDataDTO = {
      title: 'Confirmar eliminación',
      message: `¿Estás seguro de que deseas eliminar el namespace "${namespace.name}"? Esta acción no se puede deshacer.`,
      confirmText: 'Eliminar',
      cancelText: 'Cancelar'
    };

    const dialogRef = this.dialog.open(ConfirmDeleteDialogComponent, {
      width: '500px',
      data: dialogData
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.confirmDeleteNamespace(namespace);
      }
    });
  }

  confirmDeleteNamespace(namespace: Namespace) {
    this.namespaceService.deleteByZrn(namespace.zrn).subscribe({
      next: (response) => {
        if (response.success) {
          this.namespaceService.setChangeObjectDelete(namespace);
          this.getAllNamespaces();
          console.log('Namespace deleted successfully:', namespace.name);
        } else {
          console.error('Failed to delete namespace:', response.message);
        }
      }
      , error: (error) => {
        console.error('Error deleting namespace:', error);
      }
    });
  }

}
