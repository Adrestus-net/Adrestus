# GitHub Actions Workflows

## PR Labeler

The PR Labeler workflow automatically applies labels to pull requests based on the files changed.

### Security Considerations

This implementation addresses security concerns identified in similar workflows:

1. **Uses `pull_request` instead of `pull_request_target`**: The workflow uses the `pull_request` event to avoid security risks associated with running untrusted code from PRs with elevated permissions. This is the recommended approach for labeling workflows.

2. **No checkout of untrusted code**: By using the `pull_request` event, the workflow automatically checks out the base branch, avoiding the security risk of executing potentially malicious code from PRs.

3. **Fork compatibility**: Works correctly with PRs from forked repositories without requiring special configuration.

### Configuration

Labels are automatically applied based on file patterns defined in `.github/labeler.yml`. The configuration uses the actions/labeler@v5 schema with array matchers.

Available labels:
- Module labels: `api`, `bloom-filter`, `config`, `consensus`, `core`, `crypto`, `distributed-ledger`, `erasure-code`, `network`, `protocol`, `shared-resources`, `trie`, `util`
- Functional labels: `documentation`, `ci`, `docker`, `build`, `tests`
