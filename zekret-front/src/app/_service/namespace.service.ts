import { Injectable } from '@angular/core';
import { Namespace } from '../_model/namespace';
import { GenericService } from './generic.service';
import { environment } from '../../environments/environment.development';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class NamespaceService extends GenericService<Namespace> {

  constructor(protected override http: HttpClient) {
    super(
      http,
      `${environment.apiUrl}/namespaces`
    )
   }
}
