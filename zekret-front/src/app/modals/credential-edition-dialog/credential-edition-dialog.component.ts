import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { credentialType } from '../../_model/credential-type';

@Component({
  selector: 'app-credential-edition-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './credential-edition-dialog.component.html',
  styleUrl: './credential-edition-dialog.component.css'
})
export class CredentialEditionDialogComponent {

  credentialType = credentialType;

  credentialForm: FormGroup;

  constructor() {
    this.credentialForm = new FormGroup({
      title: new FormControl('', [Validators.required]),
      type: new FormControl(credentialType[0].key, [Validators.required]),
      credentialValue: new FormControl('', [Validators.required]),
      
      up_username: new FormControl('', [Validators.required]),
      up_password: new FormControl('', [Validators.required]),

      ssh_private_key: new FormControl(''),

      secret_text: new FormControl(''),

      file_name: new FormControl(''),
      file_content: new FormControl(''),

      notas: new FormControl(''),
      
    });
  }

  onTypeChange(event: any) {
    const selectedType = event.target.value;
    this.credentialForm.get('up_username')?.clearValidators();
      this.credentialForm.get('up_password')?.clearValidators();
    this.credentialForm.get('ssh_private_key')?.clearValidators();
    this.credentialForm.get('secret_text')?.clearValidators();
    this.credentialForm.get('file_name')?.clearValidators();
    this.credentialForm.get('file_content')?.clearValidators();
    switch (selectedType) {
      case 'username_password':
        this.credentialForm.get('up_username')?.setValidators([Validators.required]);
        this.credentialForm.get('up_password')?.setValidators([Validators.required]);
        break;
      case 'ssh_username':
        this.credentialForm.get('up_username')?.setValidators([Validators.required]);
        this.credentialForm.get('ssh_private_key')?.setValidators([Validators.required]);
        break;
      case 'secret_text':
        this.credentialForm.get('secret_text')?.setValidators([Validators.required]);
        break;
      case 'file':
        this.credentialForm.get('file_name')?.setValidators([Validators.required]);
        this.credentialForm.get('file_content')?.setValidators([Validators.required]);
        break;
    }
  }

  isTypeSelected(type: string): boolean {
    return this.credentialForm.get('type')?.value === type;
  }

  onSubmit() {
    if (this.credentialForm.valid) {
      console.log('Form submitted with values:', this.credentialForm.value);
      // Here you would typically send the form data to your backend service
    } else {
      console.error('Form is invalid');
    }
  }
}
