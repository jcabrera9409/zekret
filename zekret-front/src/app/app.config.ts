import { ApplicationConfig, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';

import { JwtModule } from '@auth0/angular-jwt';

import { routes } from './app.routes';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { UtilMethods } from './util/util';
import { environment } from '../environments/environment.development';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

const jwtConfig = {
  config: {
    tokenGetter: UtilMethods.getJwtToken,
    allowedDomains: environment.domains,
    disallowedRoutes: environment.disallowedRoutes
  }
}

export const appConfig: ApplicationConfig = {
  providers: [
    importProvidersFrom(
      JwtModule.forRoot(jwtConfig),
    ),
    provideHttpClient(
      withInterceptorsFromDi(),
    ),
    provideRouter(routes), 
    provideAnimationsAsync()]
};
