import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';
import { enableProdMode } from '@angular/core';
import { EnvService } from './app/_service/env.service';

// Crear instancia de EnvService para verificar modo producción
const envService = new EnvService();

if (envService.isProduction()) {
  enableProdMode();
}

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));
