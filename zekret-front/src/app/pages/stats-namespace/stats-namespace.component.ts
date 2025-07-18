import { Component } from '@angular/core';
import { NamespaceService } from '../../_service/namespace.service';
import { CredentialService } from '../../_service/credential.service';

@Component({
  selector: 'app-stats-namespace',
  standalone: true,
  imports: [],
  templateUrl: './stats-namespace.component.html',
  styleUrl: './stats-namespace.component.css'
})
export class StatsNamespaceComponent {

  totalNamespaces: number = 0;
  totalCredentials: number = 0;

  constructor(
    private namespaceService: NamespaceService,
    private credentialService: CredentialService
  ) { }

  ngOnInit(): void {
    this.namespaceService.getChangeObject().subscribe(namespaces => {
      this.totalNamespaces = namespaces.length;
      this.totalCredentials = namespaces.reduce((sum, ns) => sum + (ns.credentials ? ns.credentials.length : 0), 0);
    });
  }


}
