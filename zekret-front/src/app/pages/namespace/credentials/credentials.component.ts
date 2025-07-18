import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { CredentialEditionDialogComponent } from '../../../modals/credential-edition-dialog/credential-edition-dialog.component';
import { CredentialDetailDialogComponent } from '../../../modals/credential-detail-dialog/credential-detail-dialog.component';
import { Namespace } from '../../../_model/namespace';
import { Credential } from '../../../_model/credential';
import { NamespaceService } from '../../../_service/namespace.service';

@Component({
  selector: 'app-credentials',
  standalone: true,
  imports: [CommonModule, MatDialogModule],
  templateUrl: './credentials.component.html',
  styleUrl: './credentials.component.css'
})
export class CredentialsComponent implements OnChanges {
  @Input() selectedNamespace: Namespace | null = null;

  constructor(
    private dialog: MatDialog
  ) {}

  currentCredentials: Credential[] = [];

  ngOnChanges() {
    if (this.selectedNamespace) {
      this.loadCredentialsForNamespace(this.selectedNamespace);
    }
  }

  openCreateCredentialDialog() {
    this.dialog.open(CredentialEditionDialogComponent, {
      width: '500px',
      data: { namespace: this.selectedNamespace }
    });
  }

  onCredentialClick(credential: Credential) {
    this.dialog.open(CredentialDetailDialogComponent, {
      width: '800px',
      data: credential
    });
  }

  private loadCredentialsForNamespace(namespace: Namespace) {
    this.currentCredentials = namespace.credentials || [];
  }
}
