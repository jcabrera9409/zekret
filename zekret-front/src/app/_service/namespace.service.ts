import { Injectable } from '@angular/core';
import { NamespaceRequestDTO, NamespaceResponseDTO } from '../_model/namespace';
import { GenericService } from './generic.service';
import { HttpClient } from '@angular/common/http';
import { EnvService } from './env.service';

@Injectable({
  providedIn: 'root'
})
export class NamespaceService extends GenericService<NamespaceRequestDTO, NamespaceResponseDTO> {

  constructor(
    protected override http: HttpClient,
    private envService: EnvService
  ) {
    const apiUrl = envService.getApiUrl();
    super(
      http,
      `${apiUrl}/namespaces`
    )
   }
}
