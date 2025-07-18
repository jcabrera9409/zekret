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

@Component({
  selector: 'app-credentials',
  standalone: true,
  imports: [CommonModule, MatDialogModule],
  templateUrl: './credentials.component.html',
  styleUrl: './credentials.component.css'
})
export class CredentialsComponent implements OnChanges, OnInit {
  @Input() selectedNamespace: Namespace | null = null;

  constructor(
    private dialog: MatDialog,
    private credentialService: CredentialService
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

    this.credentialService.getAllByNamespaceZrn(this.selectedNamespace.zrn).subscribe({
      next: (data) => {
        this.currentCredentials = data.data;
      }, error: (error) => {
        console.error('Error fetching credentials:', error);
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
      width: '800px',
      data: credential
    });
  }

  private confirmDeleteNamespace(credencial: Credential) {
    this.credentialService.deleteByZrn(credencial.zrn).subscribe({
      next: (response) => {
        if (response.success) {
          this.credentialService.setChangeObjectDelete(credencial);
          console.log('Credential deleted successfully:', credencial.title);
        } else {
          console.error('Failed to delete credential:', response.message);
        }
      }
      , error: (error) => {
        console.error('Error deleting credemtial:', error);
      }
    });
  }

}
