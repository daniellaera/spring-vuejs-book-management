import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ProgressSpinnerModule } from 'primeng/progressspinner';

import { GithubService } from '../../services/github.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-github-callback',
  standalone: true,
  imports: [CommonModule, ProgressSpinnerModule],
  templateUrl: './github-callback.html',
  styleUrl: './github-callback.scss'
})
export class GithubCallback implements OnInit {
  constructor(
    private githubService: GithubService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    console.log('🔍 GitHub Callback component loaded');
    console.log('🔍 Full URL:', window.location.href);

    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get('code');

    console.log('🔍 Auth code:', code);

    if (!code) {
      console.error('❌ No authorization code found in URL');
      this.router.navigate(['/login']);
      return;
    }

    console.log('✅ Calling GitHub callback API...');
    this.githubService.handleGithubCallback(code).subscribe({
      next: (response) => {
        console.log('✅ GitHub callback response:', response);
        const token = response.token;

        if (!token) {
          console.error('❌ Token is missing in the response');
          this.router.navigate(['/login']);
          return;
        }

        console.log('✅ Token received, storing...');
        localStorage.setItem('auth_token', token);
        localStorage.setItem('username', response.username);
        this.authService.checkSession();

        console.log('✅ Redirecting to home...');
        this.router.navigate(['/']);
      },
      error: (error) => {
        console.error('❌ GitHub OAuth processing failed:', error);
        alert('Login failed. Please try again.');
        this.router.navigate(['/login']);
      }
    });
  }
}
