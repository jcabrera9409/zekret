import { Component, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { NamespaceEditionDialogComponent } from '../../../modals/namespace-edition-dialog/namespace-edition-dialog.component';
import { ConfirmDeleteDialogComponent } from '../../../modals/confirm-delete-dialog/confirm-delete-dialog.component';
import { NamespaceService } from '../../../_service/namespace.service';
import { NamespaceRequestDTO, NamespaceResponseDTO } from '../../../_model/namespace';
import { ConfirmDeleteDataDTO } from '../../../_model/dto';
import { CredentialService } from '../../../_service/credential.service';
import { LoaderComponent } from "../../../shared/loader/loader.component";
import { NotificationService } from '../../../_service/notification.service';
import { Message } from '../../../_model/message';
import { finalize } from 'rxjs';


@Component({
  selector: 'app-index-namespace',
  standalone: true,
  imports: [CommonModule, MatDialogModule, LoaderComponent],
  templateUrl: './index-namespace.component.html',
  styleUrl: './index-namespace.component.css'
})
export class IndexNamespaceComponent implements OnInit {
  @Output() namespaceSelected = new EventEmitter<NamespaceResponseDTO>();

  isLoading: boolean = false;

  namespaces: NamespaceResponseDTO[] = [];

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
    });

    this.credentialService.getChangeObject().subscribe({
      next: () => {
        this.getAllNamespaces(); 
      }
    });

    this.credentialService.getChangeObjectDelete().subscribe({
      next: () => {
        this.getAllNamespaces();
      }
    });
  }

  getAllNamespaces() {
    this.isLoading = true;
    this.namespaceService.getAll()
      .pipe(
        finalize(() => {
          this.isLoading = false;
        })
      )
      .subscribe((response) => {
        if (response.success) {
          this.namespaceService.setChangeObject(response.data);
        } else {
          this.namespaceService.setChangeObject([]);
          this.notificationService.setMessageChange(
            Message.error('Error al cargar los namespaces', response)
          );
        }
      })
  }

  onNamespaceClick(namespace: NamespaceResponseDTO) {
    this.namespaceSelected.emit(namespace);
  }

  openCreateNamespaceDialog(namespace: NamespaceRequestDTO = null) {
    this.dialog.open(NamespaceEditionDialogComponent, {
      width: '500px',
      data: namespace
    });
  }

  deleteNamespace(namespace: NamespaceResponseDTO) {
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

  private confirmDeleteNamespace(namespace: NamespaceResponseDTO) {
    this.isLoading = true;
    this.namespaceService.deleteByZrn(namespace.zrn)
      .pipe(
        finalize(() => {
          this.isLoading = false;
        })
      )
      .subscribe((response) => {
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
      })
  }

}
