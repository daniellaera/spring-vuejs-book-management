import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';

// PrimeNG
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { DividerModule } from 'primeng/divider';

import { AuthService } from '../../services/auth.service';
import {GithubService} from '../../services/github.service';

@Component({
  selector: 'app-auth-form',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CardModule,
    InputTextModule,
    PasswordModule,
    ButtonModule,
    MessageModule,
    DividerModule
  ],
  templateUrl: './auth-form.html',
  styleUrl: './auth-form.scss'
})
export class AuthForm implements OnInit {
  isLoginMode = true; // true = login, false = signup

  email = signal('');
  password = signal('');
  errorMessage = signal<string | null>(null);
  loading = signal(false);

  emailError = signal('');
  passwordError = signal('');
  oauth2Enabled = signal(false);

  constructor(
    private authService: AuthService,
    private githubService: GithubService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Determine mode from route
    this.isLoginMode = this.route.snapshot.url[0]?.path === 'login';

    // Check if GitHub OAuth is enabled
    this.githubService.isGithubEnabled().subscribe({
      next: (response) => {
        this.oauth2Enabled.set(response.oauth2Enabled);
      },
      error: (error) => {
        console.error('Failed to fetch OAuth2 status:', error);
      }
    });
  }

  validateEmail(): void {
    const emailValue = this.email();
    if (!emailValue) {
      this.emailError.set('Email is required');
    } else if (!/\S+@\S+\.\S+/.test(emailValue)) {
      this.emailError.set('Invalid email format');
    } else {
      this.emailError.set('');
    }
  }

  validatePassword(): void {
    const passwordValue = this.password();
    if (!passwordValue) {
      this.passwordError.set('Password is required');
    } else if (!this.isLoginMode && passwordValue.length < 6) {
      this.passwordError.set('Password must be at least 6 characters');
    } else {
      this.passwordError.set('');
    }
  }

  isFormValid(): boolean {
    return !!(
      this.email() &&
      this.password() &&
      !this.emailError() &&
      !this.passwordError()
    );
  }

  handleSubmit(): void {
    this.validateEmail();
    this.validatePassword();

    if (!this.isFormValid()) return;

    this.loading.set(true);
    this.errorMessage.set(null);

    if (this.isLoginMode) {
      this.handleLogin();
    } else {
      this.handleSignup();
    }
  }

  handleLogin(): void {
    this.authService.login(this.email(), this.password()).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/']);
      },
      error: (error) => {
        this.loading.set(false);
        this.errorMessage.set(
          error.error?.message || 'Invalid email or password'
        );
      }
    });
  }

  handleSignup(): void {
    this.authService.signup(this.email(), this.password()).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/login']);
      },
      error: (error) => {
        this.loading.set(false);
        this.errorMessage.set(
          error.error?.message || 'Failed to create account'
        );
      }
    });
  }

  loginWithGitHub(): void {
    this.githubService.getGithubAuthUrl().subscribe({
      next: (response) => {
        // Redirect to GitHub OAuth
        window.location.href = response.authUrl;
      },
      error: (error) => {
        console.error('GitHub OAuth redirect failed:', error);
        this.errorMessage.set('GitHub login unavailable');
      }
    });
  }

  switchMode(): void {
    if (this.isLoginMode) {
      this.router.navigate(['/signup']);
    } else {
      this.router.navigate(['/login']);
    }
  }
}
