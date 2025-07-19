import { Component, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { NamespaceEditionDialogComponent } from '../../../modals/namespace-edition-dialog/namespace-edition-dialog.component';
import { ConfirmDeleteDialogComponent } from '../../../modals/confirm-delete-dialog/confirm-delete-dialog.component';
import { NamespaceService } from '../../../_service/namespace.service';
import { Namespace } from '../../../_model/namespace';
import { ConfirmDeleteDataDTO } from '../../../_model/dto';
import { CredentialService } from '../../../_service/credential.service';
import { LoaderComponent } from "../../../shared/loader/loader.component";
import { NotificationService } from '../../../_service/notification.service';
import { Message } from '../../../_model/message';


@Component({
  selector: 'app-index-namespace',
  standalone: true,
  imports: [CommonModule, MatDialogModule, LoaderComponent],
  templateUrl: './index-namespace.component.html',
  styleUrl: './index-namespace.component.css'
})
export class IndexNamespaceComponent implements OnInit {
  @Output() namespaceSelected = new EventEmitter<Namespace>();

  isLoading: boolean = false;

  namespaces: Namespace[] = [];

  constructor(
    private dialog: MatDialog,
    private namespaceService: NamespaceService,
    private credentialService: CredentialService,
    private notificationService: NotificationService
  ){}

  ngOnInit(): void {
    this.getAllNamespaces();
    this.namespaceService.getChangeObject().subscribe({
      next: (namespaces) => {
        this.namespaces = namespaces;
      }
      , error: (error) => {
        console.error('Error updating namespaces:', error);
      }
    });

    this.credentialService.getChangeObject().subscribe({
      next: () => {
        this.getAllNamespaces(); 
      }, error: (error) => {
        console.error('Error updating credentials:', error);
      }
    });

    this.credentialService.getChangeObjectDelete().subscribe({
      next: () => {
        this.getAllNamespaces();
      }
      , error: (error) => {
        console.error('Error deleting credential:', error);
      }
    });
  }

  getAllNamespaces() {
    this.isLoading = true;
    this.namespaceService.getAll().subscribe({
      next: (response) => {
        if (response.success) {
          this.namespaceService.setChangeObject(response.data);
        } else {
          this.namespaceService.setChangeObject([]);
          console.error('Failed to load namespaces:', response.message);
        }
        this.isLoading = false;
      }
      , error: (error) => {
        this.namespaceService.setChangeObject([]);
        console.error('Error fetching namespaces:', error);
        this.isLoading = false;
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

  private confirmDeleteNamespace(namespace: Namespace) {
    this.isLoading = true;
    this.namespaceService.deleteByZrn(namespace.zrn).subscribe({
      next: (response) => {
        if (response.success) {
          this.namespaceService.setChangeObjectDelete(namespace);
          this.getAllNamespaces();
          this.notificationService.setMessageChange(
            Message.success('Namespace eliminado correctamente')
          );
        } else {
          this.notificationService.setMessageChange(
            Message.error('Error al eliminar el namespace', response.message)
          );
        }
        this.isLoading = false;
      }
      , error: (error) => {
        this.notificationService.setMessageChange(
          Message.error('Ocurrió un error al eliminar el namespace', error)
        );
        this.isLoading = false;
      }
    });
  }

}
