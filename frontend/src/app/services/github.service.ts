import {Injectable} from '@angular/core';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {FeaturesResponse} from '../models/features-response.model';
import {GitHubAuthResponse} from '../models/github-auth-response.model';
import {GitHubCallbackResponse} from '../models/github-callback-response.model';

@Injectable({
  providedIn: 'root',
})
export class GithubService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {
  }

  // Check if GitHub OAuth is enabled
  isGithubEnabled(): Observable<FeaturesResponse> {
    return this.http.get<FeaturesResponse>(`${this.apiUrl}/features`);
  }

  // Get GitHub OAuth URL
  getGithubAuthUrl(): Observable<GitHubAuthResponse> {
    return this.http.get<GitHubAuthResponse>(`${this.apiUrl}/github/login`);
  }

  // Handle GitHub callback with code
  handleGithubCallback(code: string): Observable<GitHubCallbackResponse> {
    return this.http.post<GitHubCallbackResponse>(`${this.apiUrl}/github/callback`, {code});
  }
}
