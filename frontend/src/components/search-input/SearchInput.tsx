import './SearchInput.css';

export function SearchInput({ value, onChange, placeholder = 'Search books by title' }: {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
}) {
  return (
    <label className="search-input">
      <span className="sr-only">Search books</span>
      <input type="search" value={value} placeholder={placeholder}
        onChange={event => onChange(event.target.value)} />
    </label>
  );
}
