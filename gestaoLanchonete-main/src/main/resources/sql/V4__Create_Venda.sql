CREATE TABLE IF NOT EXISTS Venda (
    id INT PRIMARY KEY,
    produto VARCHAR(255) NOT NULL,
    quantidade INT NOT NULL,
    precoTotal DOUBLE NOT NULL
);
