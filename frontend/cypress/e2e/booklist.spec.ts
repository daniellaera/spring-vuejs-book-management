/// <reference types="cypress" />

describe('BookList', () => {
  beforeEach(() => {
    // Mock the API call to /book and add a delay
    cy.intercept('GET', '/api/v3/book', (req) => {
      req.reply({
        fixture: 'books.json',
        delayMs: 1000, // Add a delay of 1 second to simulate loading
      });
    }).as('getBooks');

    // Visit the page
    cy.visit('/'); // Adjust the URL as per your routing setup
  });

  it('displays a loading message initially', () => {
    // Ensure the loading message is visible before the API responds
    cy.get('.loading-message').should('be.visible').and('contain', 'Loading books...');
  });

  it('displays a list of books', () => {
    // Wait for the mock response to be fetched
    cy.wait('@getBooks', { timeout: 10000 });

    // Ensure rows are displayed after the API responds
    cy.get('.book-table tbody tr').should('have.length.greaterThan', 0);
  });

  it('displays correct book data', () => {
    cy.wait('@getBooks', { timeout: 10000 });

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
