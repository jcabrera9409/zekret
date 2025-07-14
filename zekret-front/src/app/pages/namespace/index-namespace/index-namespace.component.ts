import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-index-namespace',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './index-namespace.component.html',
  styleUrl: './index-namespace.component.css'
})
export class IndexNamespaceComponent {
  @Output() namespaceSelected = new EventEmitter<string>();

  // Datos de ejemplo - después puedes reemplazar con datos reales de un servicio
  namespaces = [
    {
      name: 'Desarrollo',
      description: 'Credenciales para el entorno de desarrollo',
      credentialsCount: 5
    },
    {
      name: 'Producción',
      description: 'Credenciales para el entorno de producción',
      credentialsCount: 12
    },
    {
      name: 'Testing',
      description: 'Credenciales para el entorno de testing',
      credentialsCount: 3
    }
  ];

  onNamespaceClick(namespaceName: string) {
    this.namespaceSelected.emit(namespaceName);
  }
}
