import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from "../header/header.component";
import { StatsNamespaceComponent } from "../stats-namespace/stats-namespace.component";
import { IndexNamespaceComponent } from "./index-namespace/index-namespace.component";
import { CredentialsComponent } from "./credentials/credentials.component";
import { NamespaceResponseDTO } from '../../_model/namespace';
import { NamespaceService } from '../../_service/namespace.service';
import { LoaderComponent } from "../../shared/loader/loader.component";

@Component({
  selector: 'app-namespace',
  standalone: true,
  imports: [CommonModule, HeaderComponent, StatsNamespaceComponent, IndexNamespaceComponent, CredentialsComponent, LoaderComponent],
  templateUrl: './namespace.component.html',
  styleUrl: './namespace.component.css'
})
export class NamespaceComponent implements OnInit {

  isLoading: boolean = false;
  isNamespacesTabActive: boolean = true;
  isCredentialsTabActive: boolean = false;

  selectedNamespace: NamespaceResponseDTO | null = null;

  constructor(
    private namespaceService: NamespaceService
  ) {}

  ngOnInit(): void {
      this.namespaceService.getChangeObjectDelete().subscribe({
      next: (namespace) => {
        if (this.selectedNamespace && this.selectedNamespace.zrn === namespace.zrn) {
          this.selectedNamespace = null;
        }
      }
    });
  }

  onNamespaceSelected(namespace: NamespaceResponseDTO) {
    this.selectedNamespace = namespace;
    this.toggleCredentialsTab(); 
  }

  toggleNamespacesTab() {
    this.isNamespacesTabActive = true;
    this.isCredentialsTabActive = false;
  }

  toggleCredentialsTab() {
    this.isNamespacesTabActive = false;
    this.isCredentialsTabActive = true;
  }
}
