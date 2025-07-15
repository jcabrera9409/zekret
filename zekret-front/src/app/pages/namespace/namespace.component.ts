import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from "../header/header.component";
import { StatsNamespaceComponent } from "../stats-namespace/stats-namespace.component";
import { IndexNamespaceComponent } from "./index-namespace/index-namespace.component";
import { CredentialsComponent } from "./credentials/credentials.component";

@Component({
  selector: 'app-namespace',
  standalone: true,
  imports: [CommonModule, HeaderComponent, StatsNamespaceComponent, IndexNamespaceComponent, CredentialsComponent],
  templateUrl: './namespace.component.html',
  styleUrl: './namespace.component.css'
})
export class NamespaceComponent {

  isLoading: boolean = false;
  isNamespacesTabActive: boolean = true;
  isCredentialsTabActive: boolean = false;

  selectedNamespace: string | null = null;

  onNamespaceSelected(namespace: string) {
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
