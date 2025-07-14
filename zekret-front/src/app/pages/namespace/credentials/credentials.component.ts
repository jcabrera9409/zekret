import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-credentials',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './credentials.component.html',
  styleUrl: './credentials.component.css'
})
export class CredentialsComponent implements OnChanges {
  @Input() selectedNamespace: string | null = null;

  // Datos de ejemplo de credenciales por namespace
  credentialsByNamespace: { [key: string]: any[] } = {
    'Desarrollo': [
      { name: 'DB_DEV', type: 'Database', value: '***', lastModified: '2024-01-15' },
      { name: 'API_KEY_DEV', type: 'API Key', value: '***', lastModified: '2024-01-10' },
      { name: 'AWS_SECRET_DEV', type: 'AWS', value: '***', lastModified: '2024-01-12' }
    ],
    'Producción': [
      { name: 'DB_PROD', type: 'Database', value: '***', lastModified: '2024-01-20' },
      { name: 'API_KEY_PROD', type: 'API Key', value: '***', lastModified: '2024-01-18' },
      { name: 'AWS_SECRET_PROD', type: 'AWS', value: '***', lastModified: '2024-01-19' }
    ],
    'Testing': [
      { name: 'DB_TEST', type: 'Database', value: '***', lastModified: '2024-01-14' }
    ]
  };

  currentCredentials: any[] = [];

  ngOnChanges() {
    if (this.selectedNamespace) {
      this.loadCredentialsForNamespace(this.selectedNamespace);
    }
  }

  private loadCredentialsForNamespace(namespace: string) {
    this.currentCredentials = this.credentialsByNamespace[namespace] || [];
  }
}
