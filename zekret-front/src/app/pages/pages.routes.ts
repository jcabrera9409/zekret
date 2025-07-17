import { Routes } from '@angular/router';
import { NamespaceComponent } from './namespace/namespace.component';
import { authGuard } from '../_service/guard.service';

export const pagesRoutes: Routes = [
    { path: '', component: NamespaceComponent, canActivate: [authGuard] },
];