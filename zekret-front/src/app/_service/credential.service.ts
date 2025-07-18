import { Injectable } from '@angular/core';
import { GenericService } from './generic.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment.development';
import { Credential } from '../_model/credential';
import { APIResponseDTO } from '../_model/dto';

@Injectable({
  providedIn: 'root'
})
export class CredentialService extends GenericService<Credential> {

  constructor(protected override http: HttpClient) {
    super(
      http,
      `${environment.apiUrl}/credentials`
    )
  }

  getAllByNamespaceZrn(namespaceZrn: string) {
    return this.http.get<APIResponseDTO<Credential[]>>(`${this.url}/namespace/${namespaceZrn}`);
  }
}
