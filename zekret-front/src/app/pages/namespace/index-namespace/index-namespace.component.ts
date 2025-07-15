import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { NamespaceEditionDialogComponent } from '../../../modals/namespace-edition-dialog/namespace-edition-dialog.component';


@Component({
  selector: 'app-index-namespace',
  standalone: true,
  imports: [CommonModule, MatDialogModule],
  templateUrl: './index-namespace.component.html',
  styleUrl: './index-namespace.component.css'
})
export class IndexNamespaceComponent {
  @Output() namespaceSelected = new EventEmitter<string>();

  constructor(public dialog: MatDialog){}

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

  openCreateNamespaceDialog() {
    this.dialog.open(NamespaceEditionDialogComponent, {
      width: '500px',
      data: {}
    });
  }
}
