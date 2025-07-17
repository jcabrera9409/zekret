import { Component, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { NamespaceEditionDialogComponent } from '../../../modals/namespace-edition-dialog/namespace-edition-dialog.component';
import { NamespaceService } from '../../../_service/namespace.service';
import { Namespace } from '../../../_model/namespace';
import { Message } from '../../../_model/message';


@Component({
  selector: 'app-index-namespace',
  standalone: true,
  imports: [CommonModule, MatDialogModule],
  templateUrl: './index-namespace.component.html',
  styleUrl: './index-namespace.component.css'
})
export class IndexNamespaceComponent implements OnInit {
  @Output() namespaceSelected = new EventEmitter<string>();

  namespaces: Namespace[] = [];

  constructor(
    private namespaceService: NamespaceService,
    private dialog: MatDialog
  ){}

  ngOnInit(): void {
    this.getAllNamespaces();
  }

  getAllNamespaces() {
    this.namespaceService.getAll().subscribe({
      next: (response) => {
        if (response.success) {
          this.namespaces = response.data;
          this.namespaceService.setChangeObject(this.namespaces);
          console.log('Namespaces loaded successfully:', this.namespaces);
        } else {
          this.namespaceService.setChangeObject([]);
          console.error('Failed to load namespaces:', response.message);
        }
      }
      , error: (error) => {
        this.namespaceService.setChangeObject([]);
        console.error('Error fetching namespaces:', error);
      }
    });
  }

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
