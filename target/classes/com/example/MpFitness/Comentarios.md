Crie esse arquivo pois existem comentários gerais sobre o código, estão todos os comentários da atividade nesse arquivo mas tirando os gerais como dito anteriormente, os outros sobre os códigos e sobre classes estão no código do projeto. Esse arquivo é so para ver os comentarios gerais e se quiser se guiar para ver as classes com os comentários pode usar este arquivo também. : D

----------------------------------------------------------------------------------------------------------------------------------
CODE REVIEW

1- Produto

// @Builder poderia ter sido utilizado, para manter a imutabilidade das variaveis: tipoOferta, categoria, status, emOferta

------------------------------------------------------------------------------------------------------------------------

2- Cliente Service

2- public Cliente findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id do cliente <UNK> null"); /*
            Atualemente, essa verificação pode ser desnecessária, pois o método findById do JpaRepository já lida com nulls.
            E também seria bom lançar uma exeção mais especifica, como throw new IllegalArgumentException("id do cliente não pode ser nulo")
            
            */
        }
        return clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("id do cliente <UNK> null"));
    }

------------------------------------------------------------------------------------------------------------------

3- Cliente Service 

/* Faltou o método atualizarConta, para manter a toda a lógica na services por exemplo
 * public Cliente atualizarConta(Long id, Cliente clienteAtualizado) {
 *   Cliente clienteExistente = findById(id);
 *  clienteExistente.setNome(clienteAtualizado.getNome());
 * clienteExistente.setEmail(clienteAtualizado.getEmail());
 * clienteExistente.setEndereco(clienteAtualizado.getEndereco());
 * clienteExistente.setTelefone(clienteAtualizado.getTelefone());
 * return clienteRepository.save(clienteExistente);
 */
----------------------------------------------------------------------------------------------------------------

4- Cliente controller atualizar

  /* Este método poderia ser criado na services, deixando a controller chamando esse método na service e lidando com requisições HTTP.   
         * Um dos beneficios dessa mudança é que a lógica de negócio fica toda na services, deixando a controller mais limpa e focada em lidar com requisições HTTP.
         * Outro benefício é que facilita a reutilização do código, caso outras partes da aplicação precise de um método de atualizar o cliente em outro contexto que não seja nessa controller.
        */



------------------------------------------------------------------------------------------------------------------------

5- Cliente services excluir

/*Faltou o método excluirCliente, para manter toda a lógica de negócios na services por exemplo
  * public void excluirCliente(Long id) {
  *   Cliente clienteExistente = findById(id);
    *   clienteRepository.delete(clienteExistente);
    * }
  */

---------------------------------------------------------------------------------------------------------------------

6- Cliente controller excluir


 /* Assim como o método atualizar, esse método poderia ser criado na services, deixando a controller chamando esse método na service e lidando com requisições HTTP.  Isso assim como no atualizar seria uma decisão muito importante já que é uma boa prática deixar as regras de négocio do projeto nas classes do pacote services
         * 
         * 
         */

--------------------------------------------------------------------------------------------------------------

7- Endereço.java

 /*Ná variavel String cep eu acho que uma boa decisão seria usar @Size(min = 8, max = 8) já que isso preveniria o usuário de colocar um cep que existem mais caracteres que o permitido exemplo: Se um usuário digitar o cep dessa forma 11111-111 é um formato válido pois existe 8 números + o hífen que da 9 caracteres, se não especificar a quantidade permitida o usuário pode sem querer cometendo um erro e digitando um número com mais caracteres 11111-1111*/

-------------------------------------------------------------------------------------------------------------------

8- Produto Controller

/* Uma boa prática seria deixar este método na services, para manter toda a lógica de negócios em apenas um lugar já que se precisar de listar as categorias em outra parte do código você poderia chamar ele na services e assim evitando a cópia do mesmo código de forma desnecessária um exemplo para chamar na controller seria colocar esse método na sua services tirando apenas o "return ResponseEntity.ok(categorias)" e colocando esse no lugar por exemplo: @GetMapping("/categorias")
        public ResponseEntity<List<String>> getCategorias() {
        List<String> categorias = produtoService.getCategorias();
        return ResponseEntity.ok(categorias);
}
 */

------------------------------------------------------------------------------------------------------------------------------------


9- Facade

/*Um bom padrão de projeto e decisão arquitetural seria utilizar o padrão facade para centralizar e comandar chamadas a multiplos serviços, simplificando a lógica complexa que envolveria várias operações numa controller. Isso mantém a controller limpa, a service focada em regras de negócio e facilita manutenção, testes e futuras alterações na sequência de operações. */

----------------------------------------------------------------------------------------------------------------------------------

10- Cliente.java

 /* Seria bom criar uma classe so para Enum já que se você precisar citar novamente a Role em outra parte do projeto você não precisa duplicar o Enum. Outro exemplo é se você criasse um novo tipo de role no projeto por exemplo CLIENTE_VIP você alteraria apenas a classe Enum facilitando a manunteção */

-----------------------------------------------------------------------------------------------------------------------------------

11- PedidoController.java

/* Esta controller está muito grande seria uma boa prática dividir em outras controllers exemplo:
 * - PedidoClienteController: para lidar com endpoints relacionados a pedidos feitos por clientes (finalizar compra, listar pedidos do cliente, detalhar pedido do cliente)
 * - PedidoAdminController: para lidar com endpoints administrativos relacionados a pedidos (listar todos os pedidos, atualizar status, adicionar rastreamento, etc)
 * - RelatorioPedidoController: para lidar com endpoints relacionados a relatórios de pedidos (vendas totais, contagem por status)
 * Isso também facilitaria a manutenção e a leitura do código, além de seguir o princípio de responsabilidade única.
 */

-----------------------------------------------------------------------------------------------------------------------------------

12- Carrinho Controller

/* Acima vemos que o token está sendo extraído diretamente da controller seria melhor colocar esse método na services ou um método para centralizar a validação e extração do usuário */

-----------------------------------------------------------------------------------------------------------------------------------

13- Comentário Geral

A estrutura do projeto está muito boa, está claro onde estão as classes e os arquivos estão muito organizados.
---------------------------------------------------------------------------------------------------------------------------------

14- Comentário Geral

Em muitos projetos Java sejam eles Spring ou não, existem muitos imports que não são utilizados no código apenas um import(da classe AdminInitializar.java) não esta sendo utilizado tirando isso todos os imports são utilizados ou seja está evitando linhas de código desnecessárias
----------------------------------------------------------------------------------------------------------------------------
15- Comentário Geral

Muitas classes estão comentadas e isso é muito bom, se outro programador pegar o projeto ele terá noção do que foi feito anteriormente, um exemplo é a classe OAuth2LoginSucessHandler.java 
----------------------------------------------------------------------------------------------------------------------------------

16- Comentário Geral

Ótimo uso de DTO´s no projeto! Com isso você separa a camada de persistência da API facilitando a validação dos dados e aumentando a segurança e deixando o código mais organizado e flexível para futuras alterações



------------------------------------------------------------------------------------------------------------------------------------
17- Comentário Geral

As variáveis estão no padrão java por exemplo public String telefone (da classe Cliente.java) e também variáveis que são constantes como     private static final String CEP_ORIGEM = "35650000"; (da classe FreteService.java)

-------------------------------------------------------------------------------------------------------------------------------------
18- Comentário Geral 

Senti falta de um readme falando como inicializar o Spring, já que existem programadores que podem trabalhar no seu projeto e não sabem como inicializar a aplicação, isso facilitaria o uso e também pouparia o tempo para aprender como rodar a aplicação.




-----------------------------------------------------------------------------------------------------------------------------------

19- Exception.java

/* Seria bom criar um pacote apenas para tratar exceções. Criar exceções personalizadas para substituir RuntimeException genéricas. Isso deixaria o código mais organizado e facilitaria o tratamento de erros centralizado.  */
}


-------------------------------------------------------------------------------------------------

20- Application.properties

As propriedades da aplicação está bem estruturada e também possui uma excelente prática ao esconder URLs e chaves de APIs aumentando a segurança do projeto
