import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Credential } from '../../_model/credential';
import { CommonModule } from '@angular/common';

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
    @Inject(MAT_DIALOG_DATA) public credential: Credential
  ) {
    this.setCredentialType();
   }

  copyToClipboard(text: string) {
    navigator.clipboard.writeText(text).then(() => {
      console.log('Texto copiado al portapapeles:', text);
    }).catch(err => {
      console.error('Error al copiar al portapapeles:', err);
    });
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
