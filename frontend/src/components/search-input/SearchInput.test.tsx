import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { SearchInput } from './SearchInput';

describe('SearchInput', () => {
  it('labels the field for assistive technology', () => {
    render(<SearchInput value="" onChange={() => {}} />);
    expect(screen.getByRole('searchbox', { name: /search books/i })).toBeInTheDocument();
  });

  it('reports every keystroke to the caller', async () => {
    const onChange = vi.fn();
    render(<SearchInput value="" onChange={onChange} />);
    await userEvent.type(screen.getByRole('searchbox'), 'clean');
    expect(onChange).toHaveBeenCalledTimes(5);
    expect(onChange).toHaveBeenLastCalledWith('n');
  });

  it('shows the current value and the default placeholder', () => {
    render(<SearchInput value="pragmatic" onChange={() => {}} />);
    const input = screen.getByRole('searchbox');
    expect(input).toHaveValue('pragmatic');
    expect(input).toHaveAttribute('placeholder', 'Search books by title');
  });

  it('accepts a custom placeholder', () => {
    render(<SearchInput value="" onChange={() => {}} placeholder="Find a title" />);
    expect(screen.getByRole('searchbox')).toHaveAttribute('placeholder', 'Find a title');
  });
});
