# Briefing Técnico - AtendeJá

## Visão geral

O AtendeJá é uma aplicação full stack para agenda e fila de atendimento de prestadores locais. O sistema deve permitir cadastrar clientes, profissionais e serviços, criar e remarcar agendamentos, registrar check-in, concluir atendimentos, marcar faltas e consultar a agenda diária com filtros.

## Público-alvo

- Clínicas pequenas.
- Barbearias.
- Salões.
- Consultórios.
- Assistências técnicas.

## Problema resolvido

O projeto resolve problemas recorrentes de agenda operacional: conflito de horários, falta de controle de remarcações, cancelamentos sem rastreabilidade e pouca visibilidade da ocupação diária.

## Regra central

Um profissional não pode possuir dois agendamentos ativos em intervalos sobrepostos. A regra será implementada no backend, testada com banco real e documentada na API.

## Idioma e convenções

- Código, banco, endpoints, payloads e identificadores técnicos em inglês.
- Interface, mensagens visíveis e README principal em português PT-BR.
- Datas, horários, moeda e exemplos exibidos ao usuário seguindo convenções brasileiras.

## Entregáveis esperados

- Backend Spring Boot.
- Frontend React.
- PostgreSQL com migrations Flyway.
- Docker Compose.
- Testes automatizados.
- Swagger/OpenAPI.
- README em português PT-BR.
- ADRs com decisões técnicas principais.
