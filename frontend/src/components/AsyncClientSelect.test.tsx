import { act, fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { searchActiveCustomers } from "../services/customerService";
import { customerResponse, pageResponse } from "../test/factories";
import type { CustomerResponse } from "../types/api";
import { AsyncClientSelect } from "./AsyncClientSelect";
import { useState } from "react";

vi.mock("../services/customerService", () => ({
  searchActiveCustomers: vi.fn()
}));

const mockedSearchActiveCustomers = vi.mocked(searchActiveCustomers);

function renderSelect(onChange = vi.fn()) {
  function Harness() {
    const [value, setValue] = useState<CustomerResponse | null>(null);
    return (
      <AsyncClientSelect
        value={value}
        onChange={(customer) => {
          onChange(customer);
          setValue(customer);
        }}
        placeholder="Buscar cliente"
      />
    );
  }

  render(<Harness />);
  return onChange;
}

async function advanceDebounce(milliseconds = 300) {
  await act(async () => {
    await vi.advanceTimersByTimeAsync(milliseconds);
  });
  await act(async () => {
    await Promise.resolve();
  });
}

describe("AsyncClientSelect", () => {
  afterEach(() => {
    vi.useRealTimers();
  });

  it("does not call the API before the minimum query length", async () => {
    vi.useFakeTimers();
    renderSelect();

    fireEvent.change(screen.getByPlaceholderText("Buscar cliente"), { target: { value: "M" } });
    await advanceDebounce(400);

    expect(mockedSearchActiveCustomers).not.toHaveBeenCalled();
  });

  it("searches after debounce, displays results, selects and clears a customer", async () => {
    vi.useFakeTimers();
    const customer = customerResponse();
    mockedSearchActiveCustomers.mockResolvedValue(pageResponse([customer]));
    const onChange = renderSelect();

    fireEvent.change(screen.getByPlaceholderText("Buscar cliente"), { target: { value: "Maria" } });
    await advanceDebounce(299);
    expect(mockedSearchActiveCustomers).not.toHaveBeenCalled();

    await advanceDebounce(1);
    expect(mockedSearchActiveCustomers).toHaveBeenCalledWith("Maria", expect.objectContaining({ page: 0, size: 10 }));

    fireEvent.click(screen.getByRole("button", { name: /Maria Cliente/i }));
    expect(onChange).toHaveBeenLastCalledWith(customer);

    fireEvent.click(screen.getByRole("button", { name: /limpar cliente/i }));
    expect(onChange).toHaveBeenLastCalledWith(null);
  });

  it("shows an empty result message", async () => {
    vi.useFakeTimers();
    mockedSearchActiveCustomers.mockResolvedValue(pageResponse([]));
    renderSelect();

    fireEvent.change(screen.getByPlaceholderText("Buscar cliente"), { target: { value: "zzz" } });
    await advanceDebounce();

    expect(screen.getByText("Nenhum cliente encontrado")).toBeInTheDocument();
  });
});
