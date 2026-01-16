# Contributing to RewardHub

Thank you for considering contributing to RewardHub! This document provides guidelines for contributing to the project.

## How to Contribute

### Reporting Bugs

If you find a bug, please create an issue with:
- A clear, descriptive title
- Steps to reproduce the issue
- Expected behavior vs actual behavior
- Screenshots if applicable
- Device and Android version information

### Suggesting Features

Feature suggestions are welcome! Please:
- Check if the feature has already been requested
- Provide a clear use case
- Explain the expected behavior
- Consider implementation complexity

### Code Contributions

1. **Fork the Repository**
   ```bash
   git clone https://github.com/bhumialokesh96-netizen/Data-selling-.git
   cd Data-selling-
   ```

2. **Create a Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make Your Changes**
   - Follow the existing code style
   - Write clear commit messages
   - Add comments for complex logic
   - Update documentation if needed

4. **Test Your Changes**
   - Ensure the app builds successfully
   - Test on multiple screen sizes if UI changes
   - Verify no existing functionality is broken

5. **Submit a Pull Request**
   - Provide a clear description of changes
   - Reference any related issues
   - Include screenshots for UI changes

## Code Style Guidelines

### Kotlin
- Follow official Kotlin coding conventions
- Use meaningful variable and function names
- Prefer val over var when possible
- Use data classes for simple data holders

### Compose
- Keep composables small and focused
- Extract reusable components
- Use remember for expensive calculations
- Hoist state when appropriate

### Architecture
- Follow MVVM pattern
- Keep business logic in ViewModels
- Use Repository pattern for data access
- Separate concerns clearly

## Commit Message Guidelines

- Use present tense ("Add feature" not "Added feature")
- Use imperative mood ("Move cursor to..." not "Moves cursor to...")
- Limit first line to 72 characters
- Reference issues and pull requests when relevant

Examples:
```
Add withdrawal confirmation dialog
Fix balance calculation bug (#123)
Update README with setup instructions
Refactor authentication flow
```

## Pull Request Process

1. Update README.md with details of changes if applicable
2. Update FEATURES.md if adding new functionality
3. Ensure all tests pass
4. Request review from maintainers
5. Address review comments promptly

## Code Review

All contributions will be reviewed for:
- Code quality and style
- Performance implications
- Security considerations
- User experience impact
- Documentation completeness

## Questions?

Feel free to open an issue for any questions about contributing!

## License

By contributing, you agree that your contributions will be licensed under the same license as the project (MIT License).
