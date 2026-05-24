import { X } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import { searchActiveCustomers } from "../services/customerService";
import type { CustomerResponse } from "../types/api";
import { getApiErrorMessage } from "../utils/errors";

interface AsyncClientSelectProps {
  value: CustomerResponse | null;
  onChange: (customer: CustomerResponse | null) => void;
  placeholder?: string;
  disabled?: boolean;
  required?: boolean;
}

function formatCustomerLabel(customer: CustomerResponse): string {
  const contact = customer.email ?? customer.phone;
  return contact ? `${customer.name} - ${contact}` : customer.name;
}

export function AsyncClientSelect({
  value,
  onChange,
  placeholder = "Digite nome, telefone ou e-mail",
  disabled = false,
  required = false
}: AsyncClientSelectProps) {
  const [inputValue, setInputValue] = useState(value ? formatCustomerLabel(value) : "");
  const [options, setOptions] = useState<CustomerResponse[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const selectedIdRef = useRef<number | null>(value?.id ?? null);
  const requestIdRef = useRef(0);

  useEffect(() => {
    const selectedId = value?.id ?? null;
    if (selectedId !== selectedIdRef.current) {
      selectedIdRef.current = selectedId;
      setInputValue(value ? formatCustomerLabel(value) : "");
      setOptions([]);
      setHasSearched(false);
      setError(null);
    }
  }, [value]);

  useEffect(() => {
    const query = inputValue.trim();
    const selectedLabel = value ? formatCustomerLabel(value) : "";
    if (disabled || query.length < 2 || (value && inputValue === selectedLabel)) {
      requestIdRef.current += 1;
      setOptions([]);
      setIsLoading(false);
      setHasSearched(false);
      setError(null);
      return;
    }

    const requestId = requestIdRef.current + 1;
    requestIdRef.current = requestId;
    const controller = new AbortController();
    let isActive = true;
    const timeoutId = window.setTimeout(() => {
      setIsLoading(true);
      setError(null);
      searchActiveCustomers(query, { page: 0, size: 10, signal: controller.signal })
        .then((response) => {
          if (isActive && requestId === requestIdRef.current) {
            setOptions(response.content);
            setHasSearched(true);
          }
        })
        .catch((caught) => {
          if (!isActive || controller.signal.aborted || requestId !== requestIdRef.current) {
            return;
          }
          setOptions([]);
          setHasSearched(true);
          setError(getApiErrorMessage(caught));
        })
        .finally(() => {
          if (isActive && requestId === requestIdRef.current) {
            setIsLoading(false);
          }
        });
    }, 300);

    return () => {
      isActive = false;
      window.clearTimeout(timeoutId);
      controller.abort();
    };
  }, [disabled, inputValue, value]);

  function handleInputChange(nextValue: string) {
    setInputValue(nextValue);
    if (value) {
      selectedIdRef.current = null;
      onChange(null);
    }
  }

  function selectCustomer(customer: CustomerResponse) {
    selectedIdRef.current = customer.id;
    setInputValue(formatCustomerLabel(customer));
    setOptions([]);
    setHasSearched(false);
    setError(null);
    onChange(customer);
  }

  function clearCustomer() {
    selectedIdRef.current = null;
    setInputValue("");
    setOptions([]);
    setHasSearched(false);
    setError(null);
    onChange(null);
  }

  const shouldShowOptions = options.length > 0 && !disabled;
  const shouldShowEmpty = !isLoading && hasSearched && options.length === 0 && !error;

  return (
    <div className="async-select">
      <div className="async-select-input">
        <input
          type="text"
          value={inputValue}
          onChange={(event) => handleInputChange(event.target.value)}
          placeholder={placeholder}
          disabled={disabled}
          aria-required={required}
          autoComplete="off"
          aria-autocomplete="list"
          aria-expanded={shouldShowOptions}
        />
        {value ? (
          <button type="button" className="icon-button" onClick={clearCustomer} disabled={disabled} aria-label="Limpar cliente">
            <X size={16} />
          </button>
        ) : null}
      </div>
      {isLoading ? <div className="async-select-status" role="status">Carregando...</div> : null}
      {error ? <div className="async-select-status field-error" role="alert">{error}</div> : null}
      {shouldShowEmpty ? <div className="async-select-status" role="status">Nenhum cliente encontrado</div> : null}
      {shouldShowOptions ? (
        <ul className="async-select-options" role="listbox">
          {options.map((customer) => (
            <li key={customer.id}>
              <button type="button" onClick={() => selectCustomer(customer)}>
                <strong>{customer.name}</strong>
                <span>{customer.email ?? customer.phone}</span>
              </button>
            </li>
          ))}
        </ul>
      ) : null}
    </div>
  );
}
