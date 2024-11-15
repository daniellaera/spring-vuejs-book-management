/// <reference types="cypress" />

describe('BookList', () => {
  beforeEach(() => {
    // Mock the API call to /book and return mock data from the fixture
    cy.intercept('GET', '/api/v3/book', { fixture: 'books.json' }).as('getBooks');

    // Visit the page
    cy.visit('/'); // Adjust the URL as per your routing setup
  });

  it('displays a loading message initially', () => {
    cy.get('.loading-message').should('contain', 'Loading books...');
  });

  it('displays a list of books', () => {
    cy.wait('@getBooks', { timeout: 10000 }); // Wait for the mock response
    cy.get('.book-table tbody tr').should('have.length.greaterThan', 0); // Ensure rows are displayed
  });

  it('displays correct book data', () => {
    cy.wait('@getBooks', { timeout: 10000 }); // Wait for the mock response

    // Check the content of the first row in the table
    cy.get('.book-table tbody tr').first().within(() => {
      cy.get('td').eq(0).should('contain', '978-0987654321'); // ISBN
      cy.get('td').eq(1).should('contain', 'Advanced Java Programming'); // Title
    });
  });

  it('shows "Login to Comment" for logged-out users', () => {
    cy.wait('@getBooks', { timeout: 10000 });

    // Verify the "Comment" button is disabled for logged-out users
    cy.get('.comment-btn').first().should('be.disabled').and('contain', 'Login to Comment');
  });
});
