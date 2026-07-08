package br.com.poupacompra.scraping.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LocalMockController {

    @GetMapping("/mock/sc/validation")
    public ResponseEntity<String> validationPage() {
        String html = """
            <!DOCTYPE html>
            <html>
            <body>
              <div class=\"cf-turnstile\"></div>
              <button id=\"Body_Main_ButtonValidar\" type=\"button\">Validar</button>
              <div id=\"alerta\">Efetue a validação de segurança.</div>
              <script>
                document.addEventListener('DOMContentLoaded', function () {
                  const button = document.getElementById('Body_Main_ButtonValidar');
                  if (button) {
                    button.addEventListener('click', function () {
                      window.location.href = '/mock/sc/details?rq=mock';
                    });
                  }
                });
              </script>
            </body>
            </html>
            """;

        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(html);
    }

    @GetMapping("/mock/sc/details")
    public ResponseEntity<String> details(@RequestParam("rq") String rq) throws IOException {
        Path notaScPath = Path.of("exemplos/sc/nota-sc.html");
        String html = """
            <!DOCTYPE html>
            <html>
            <body>
              <div id=\"totalNota\">conteudo</div>
              <div id=\"infos\">
                <span class=\"chave\">1234567890123456789012345678901234567890123</span>
              </div>
              <div class=\"txtCenter\">
                <div id=\"u20\">Loja Mock SC</div>
                <div class=\"text\">CNPJ: 12.345.678/0001-99</div>
                <div class=\"text\">Rua Mock, 123 - Centro</div>
              </div>
              <table>
                <tr id=\"Item + 1\">
                  <td>
                    <span class=\"txtTit\">Produto Mock</span>
                    <span class=\"Rqtd\">Quantidade: 2</span>
                    <span class=\"RUN\">Unidade: UN</span>
                    <span class=\"RvlUnit\">Valor unitário: R$ 10,00</span>
                  </td>
                  <td><span>R$ 20,00</span></td>
                </tr>
              </table>
              <div id=\"linhaTotal\"><span>Quantidade total: 1</span></div>
              <div id=\"linhaTotal\"><span>Valor total: R$ 20,00</span></div>
            </body>
            </html>
            """;

        if (Files.exists(notaScPath)) {
            String fullHtml = Files.readString(notaScPath);
            Matcher matcher = Pattern.compile("(?is)<body[^>]*>(.*?)</body>").matcher(fullHtml);
            if (matcher.find()) {
                html = matcher.group(1);
            }
        }

        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(html);
    }
}
