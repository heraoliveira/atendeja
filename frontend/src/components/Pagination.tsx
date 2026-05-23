import type { PageResponse } from "../types/api";

interface PaginationProps {
  page: PageResponse<unknown>;
  onPageChange: (page: number) => void;
}

export function Pagination({ page, onPageChange }: PaginationProps) {
  return (
    <div className="pagination">
      <span>
        Página {page.totalPages === 0 ? 0 : page.page + 1} de {page.totalPages} · {page.totalElements} registro(s)
      </span>
      <div>
        <button type="button" className="button-secondary" disabled={page.first} onClick={() => onPageChange(page.page - 1)}>
          Anterior
        </button>
        <button type="button" className="button-secondary" disabled={page.last} onClick={() => onPageChange(page.page + 1)}>
          Próxima
        </button>
      </div>
    </div>
  );
}
