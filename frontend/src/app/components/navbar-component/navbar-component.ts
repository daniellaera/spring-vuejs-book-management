import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';

import { ToolbarModule } from 'primeng/toolbar';
import { ButtonModule } from 'primeng/button';
import { ToggleSwitchModule } from 'primeng/toggleswitch';
import { MenuModule } from 'primeng/menu';
import { BadgeModule } from 'primeng/badge';
import { MenuItem } from 'primeng/api';

import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    FormsModule,
    ToolbarModule,
    ButtonModule,
    ToggleSwitchModule,
    MenuModule,
    BadgeModule
  ],
  templateUrl: './navbar-component.html',
  styleUrl: './navbar-component.scss'
})
export class NavbarComponent implements OnInit, OnDestroy {
  isDarkMode = false;
  userMenuItems: MenuItem[] = [];

  private subscription = new Subscription();

  constructor(public authService: AuthService) {}

  ngOnInit(): void {
    // Load theme - Check localStorage first
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
      this.isDarkMode = true;
      document.documentElement.classList.add('my-app-dark');
    } else {
      this.isDarkMode = false;
      document.documentElement.classList.remove('my-app-dark');
    }

    // Update menu when auth state changes
    this.updateMenu();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  updateMenu(): void {
    if (this.isLoggedIn()) {
      this.userMenuItems = [
        {
          label: 'Profile',
          icon: 'pi pi-user',
          routerLink: ['/profile']
        },
        {
          label: 'Settings',
          icon: 'pi pi-cog',
          routerLink: ['/settings']
        },
        { separator: true },
        {
          label: 'Logout',
          icon: 'pi pi-sign-out',
          command: () => this.authService.logout()
        }
      ];
    } else {
      this.userMenuItems = [
        {
          label: 'Login',
          icon: 'pi pi-sign-in',
          routerLink: ['/login']
        },
        {
          label: 'Sign Up',
          icon: 'pi pi-user-plus',
          routerLink: ['/signup']
        }
      ];
    }
  }

  toggleTheme(): void {
    if (this.isDarkMode) {
      document.documentElement.classList.add('my-app-dark');
      localStorage.setItem('theme', 'dark');
    } else {
      document.documentElement.classList.remove('my-app-dark');
      localStorage.setItem('theme', 'light');
    }
  }

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  getTimeLeft(): number {
    return this.authService.timeLeft();
  }

  getFullName(): string {
    return this.authService.getFullName();
  }

  hasGitHub(): boolean {
    return !!this.authService.userDetails().githubId;
  }

  formatTimeLeft(): string {
    const time = this.getTimeLeft();
    const hours = Math.floor(time / 3600);
    const minutes = Math.floor((time % 3600) / 60);
    const seconds = time % 60;

    if (hours > 0) {
      return `${hours}h ${minutes.toString().padStart(2, '0')}m ${seconds.toString().padStart(2, '0')}s`;
    }
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  }
}
