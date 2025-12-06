import { Component, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UtilMethods } from '../../util/util';
import { AuthService } from '../../_service/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit{

  username: string = ''

  constructor(
    private router: Router,
    private authService: AuthService,
    private utilMethods: UtilMethods
  ) {

  }

  ngOnInit(): void {
    this.username = this.utilMethods.getUsernameFieldJwtToken();
  }

  isUserMenuOpen: boolean = false;

  toggleUserMenu() {
    this.isUserMenuOpen = !this.isUserMenuOpen;
  }

  openProfile(event: Event) {
    event.preventDefault();
    this.isUserMenuOpen = false;
    // TODO: Implementar navegación al perfil
    console.log('Abriendo perfil de usuario...');
  }

  logout() {
    this.authService.logout().subscribe({
      next: () => {
        sessionStorage.clear();
        this.router.navigate(['login']);
      },
      error: (err) => {
        console.error('Error durante el logout:', err);
        // Limpiar la sesión de todas formas
        sessionStorage.clear();
        this.router.navigate(['login']);
      }
    });
  }

  // Cerrar menú al hacer clic fuera de él
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event) {
    const target = event.target as HTMLElement;
    const userMenuBtn = document.getElementById('user-menu-btn');
    
    if (userMenuBtn && !userMenuBtn.contains(target)) {
      this.isUserMenuOpen = false;
    }
  }
}
