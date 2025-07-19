import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Credential } from '../../_model/credential';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../_service/notification.service';
import { Message } from '../../_model/message';

@Component({
  selector: 'app-credential-detail-dialog',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './credential-detail-dialog.component.html',
  styleUrl: './credential-detail-dialog.component.css'
})
export class CredentialDetailDialogComponent {

  isUsernamePassword: boolean = false;
  isSSHUsername: boolean = false;
  isSecretText: boolean = false;
  isFile: boolean = false;

  constructor(
    private notificationService: NotificationService,
    private dialogRef: MatDialogRef<CredentialDetailDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public credential: Credential
  ) {
    this.setCredentialType();
   }

  copyToClipboard(text: string) {
    navigator.clipboard.writeText(text).then(() => {
      this.notificationService.setMessageChange(
        Message.info('Texto copiado al portapapeles')
      );
    }).catch(err => {
      this.notificationService.setMessageChange(
        Message.error('Error al copiar al portapapeles', err)
      );
    });
  }

  closeDialog() {
    this.dialogRef.close();
  }

  private setCredentialType() {
    switch (this.credential.credentialType.zrn) {
      case 'username_password':
        this.isUsernamePassword = true;
        break;
      case 'ssh_username':
        this.isSSHUsername = true;
        break;
      case 'secret_text':
        this.isSecretText = true;
        break;
      case 'file':
        this.isFile = true;
        break;
    }
  }
}
