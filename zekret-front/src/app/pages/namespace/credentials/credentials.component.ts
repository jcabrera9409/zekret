import { Component, Input, OnChanges, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { CredentialEditionDialogComponent } from '../../../modals/credential-edition-dialog/credential-edition-dialog.component';
import { CredentialDetailDialogComponent } from '../../../modals/credential-detail-dialog/credential-detail-dialog.component';
import { Namespace } from '../../../_model/namespace';
import { Credential } from '../../../_model/credential';
import { CredentialService } from '../../../_service/credential.service';
import { ConfirmDeleteDialogComponent } from '../../../modals/confirm-delete-dialog/confirm-delete-dialog.component';
import { ConfirmDeleteDataDTO } from '../../../_model/dto';
import { LoaderComponent } from "../../../shared/loader/loader.component";
import { NotificationService } from '../../../_service/notification.service';
import { Message } from '../../../_model/message';

@Component({
  selector: 'app-credentials',
  standalone: true,
  imports: [CommonModule, MatDialogModule, LoaderComponent],
  templateUrl: './credentials.component.html',
  styleUrl: './credentials.component.css'
})
export class CredentialsComponent implements OnChanges, OnInit {
  @Input() selectedNamespace: Namespace | null = null;

  isLoading: boolean = false;

  constructor(
    private dialog: MatDialog,
    private credentialService: CredentialService,
    private notificationService: NotificationService
  ) {}

  currentCredentials: Credential[] = [];

  ngOnInit() {
    this.credentialService.getChangeObject().subscribe({
      next: () => {
        this.getAllCredentials();
      }, error: (error) => {
        console.error('Error updating credentials:', error);
      }
    });

    this.credentialService.getChangeObjectDelete().subscribe({
      next: () => {
        this.getAllCredentials();
      }
      , error: (error) => {
        console.error('Error deleting credential:', error);
      }
    });
  }

  getAllCredentials() {
    if (!this.selectedNamespace) {
      this.currentCredentials = [];
      return;
    }
    this.isLoading = true;

    this.credentialService.getAllByNamespaceZrn(this.selectedNamespace.zrn).subscribe({
      next: (data) => {
        this.currentCredentials = data.data;
        this.isLoading = false;
      }, error: (error) => {
        console.error('Error fetching credentials:', error);
        this.currentCredentials = [];
        this.isLoading = false;
      }
    });
  }

  ngOnChanges() {
    if (this.selectedNamespace) {
      this.getAllCredentials();
    }
  }

  openCreateCredentialDialog(credential?: Credential) {
    this.dialog.open(CredentialEditionDialogComponent, {
      width: '500px',
      data: { 
        namespace: this.selectedNamespace, 
        credential: credential || null 
      }
    });
  }

  deleteCredential(credential: Credential) {
    const dialogData: ConfirmDeleteDataDTO = {
      title: 'Confirmar eliminación',
      message: `¿Estás seguro de que deseas eliminar la credencial "${credential.title}"? Esta acción no se puede deshacer.`,
      confirmText: 'Eliminar',
      cancelText: 'Cancelar'
    };

    const dialogRef = this.dialog.open(ConfirmDeleteDialogComponent, {
      width: '500px',
      data: dialogData
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.confirmDeleteNamespace(credential);
      }
    });
  }

  onCredentialClick(credential: Credential) {
    this.dialog.open(CredentialDetailDialogComponent, {
      width: '500px',
      data: credential
    });
  }

  private confirmDeleteNamespace(credencial: Credential) {
    this.isLoading = true;
    this.credentialService.deleteByZrn(credencial.zrn).subscribe({
      next: (response) => {
        if (response.success) {
          this.credentialService.setChangeObjectDelete(credencial);
          this.notificationService.setMessageChange(
            Message.success('Credencial eliminada correctamente')
          );
        } else {
          this.notificationService.setMessageChange(
            Message.error('Error al eliminar la credencial', response.message)
          );
        }
        this.isLoading = false;
      }, error: (error) => {
        this.notificationService.setMessageChange(
          Message.error('Ocurrió un error al eliminar la credencial', error)
        );
        this.isLoading = false;
      }
    });
  }

}
