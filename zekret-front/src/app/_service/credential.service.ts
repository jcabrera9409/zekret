import { Injectable } from '@angular/core';
import { GenericService } from './generic.service';
import { HttpClient } from '@angular/common/http';
import { Credential } from '../_model/credential';
import { APIResponseDTO } from '../_model/dto';
import { EnvService } from './env.service';

@Injectable({
  providedIn: 'root'
})
export class CredentialService extends GenericService<Credential> {

  constructor(
    protected override http: HttpClient,
    private envService: EnvService
  ) {
    const apiUrl = envService.getApiUrl();
    super(
      http,
      `${apiUrl}/credentials`
    )
  }

  getAllByNamespaceZrn(namespaceZrn: string) {
    return this.http.get<APIResponseDTO<Credential[]>>(`${this.url}/namespace/${namespaceZrn}`);
  }
}
