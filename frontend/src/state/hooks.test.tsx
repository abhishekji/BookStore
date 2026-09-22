import { fireEvent, render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { beforeEach, describe, expect, it } from 'vitest';
import { useAppDispatch, useAppSelector } from './hooks';
import { clearCatalogue, markBookSelected, store } from './store';

function SelectedBooks() {
  const dispatch = useAppDispatch();
  const selected = useAppSelector(state => state.catalogue.selectedBookIds);
  return <button type="button" onClick={() => dispatch(markBookSelected('book-1'))}>Selected: {selected.length}</button>;
}

describe('typed store hooks', () => {
  beforeEach(() => store.dispatch(clearCatalogue()));

  it('reads from and dispatches to the application store', () => {
    render(<Provider store={store}><SelectedBooks /></Provider>);
    expect(screen.getByRole('button')).toHaveTextContent('Selected: 0');
    fireEvent.click(screen.getByRole('button'));
    expect(screen.getByRole('button')).toHaveTextContent('Selected: 1');
  });
});
