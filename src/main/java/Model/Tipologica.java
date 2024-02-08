package Model;

public class Tipologica {
    private String requestSottomessa;
    private String urlRichiamato;
    private String headers;
    private String responseBody;
    private String codiceRisposta;
    private String tipologicaErrore;

    public String getRequestSottomessa() {
        return requestSottomessa;
    }

    public void setRequestSottomessa(String requestSottomessa) {
        this.requestSottomessa = requestSottomessa;
    }

    public String getUrlRichiamato() {
        return urlRichiamato;
    }

    public void setUrlRichiamato(String urlRichiamato) {
        this.urlRichiamato = urlRichiamato;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getCodiceRisposta() {
        return codiceRisposta;
    }

    public void setCodiceRisposta(String codiceRisposta) {
        this.codiceRisposta = codiceRisposta;
    }

    public String getTipologicaErrore() {
        return tipologicaErrore;
    }

    public void setTipologicaErrore(String tipologicaErrore) {
        this.tipologicaErrore = tipologicaErrore;
    }

    @Override
    public String toString() {
        return "Tipologica{" +
                "requestSottomessa='" + requestSottomessa + '\'' +
                ", urlRichiamato='" + urlRichiamato + '\'' +
                ", headers='" + headers + '\'' +
                ", responseBody='" + responseBody + '\'' +
                ", codiceRisposta='" + codiceRisposta + '\'' +
                ", tipologicaErrore='" + tipologicaErrore + '\'' +
                '}';
    }
}
