CREATE TABLE elections (
  id VARCHAR(40) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (id));
CREATE TABLE election_candidate (
  election_id VARCHAR(40) NOT NULL,
  candidate_id VARCHAR(40) NOT NULL,
  votes INTEGER DEFAULT 0,
PRIMARY KEY (election_id, candidate_id));

-- SEED: Insert some data into the elections table using gemini

INSERT INTO candidates (id, photo, given_name, family_name, email, phone, job_title) VALUES
('1968984fb-441a-4fa8-875d-25d640bcc7cf', 'https://robohash.org/voluptatemquiaexcepturi.png', 'Ana', 'Santos', 'ana.santos@example.com', '+55 21 91234-5678', 'Web Developer'),
('3e4d48e2-035e-4a43-b1c6-62f4818f7e90', 'https://robohash.org/quasadoloremquevoluptatem.png', 'João', 'Silva', 'joao.silva@example.com', '+55 11 99876-5432', 'Software Engineer'),
('a4f1090a-d972-4417-98f0-76e9383e4f5e', 'https://robohash.org/quasidoloremquesint.png', 'Maria', 'Oliveira', 'maria.oliveira@example.com', '+55 31 97654-3210', 'Data Scientist'),
('7d8f3e6b-8f8d-4c40-84e3-03f1a7c9d3e7', 'https://robohash.org/etexpeditaut.png', 'Pedro', 'Pereira', 'pedro.pereira@example.com', '+55 19 96543-2109', 'Marketing Manager'),
('c7b05d0d-f66a-44d8-a5e8-e0b8a6f2c3d1', 'https://robohash.org/quasidoloremquesint.png', 'Isabella', 'Almeida', 'isabella.almeida@example.com', '+55 27 94321-0987', 'Product Designer'),
('033f7f4e-98d3-47c7-a1f5-f4c6a8b4c6e2', 'https://robohash.org/etexpeditaut.png', 'Lucas', 'Costa', 'lucas.costa@example.com', '+55 61 92109-8765', 'UI/UX Designer'),
('b8a6f2c3-d103-3f7f-4e98-d347c7a1f5f4', 'https://robohash.org/quasidoloremquesint.png', 'Mariana', 'Rodrigues', 'mariana.rodrigues@example.com', '+55 41 90987-6543', 'Front-end Developer'),
('e0b8a6f2-c3d1-c7b0-5d0d-f66a44d8a5e8', 'https://robohash.org/etexpeditaut.png', 'Gustavo', 'Ferreira', 'gustavo.ferreira@example.com', '+55 71 98765-4321', 'Back-end Developer'),
('f4c6a8b4-c6e2-033f-7f4e-98d347c7a1f5', 'https://robohash.org/quasidoloremquesint.png', 'Fernanda', 'Lima', 'fernanda.lima@example.com', '+55 81 96543-2109', 'Full-stack Developer'),
('62f4818f-7e90-3e4d-48e2-035e4a43b1c6', 'https://robohash.org/etexpeditaut.png', 'Rafael', 'Carvalho', 'rafael.carvalho@example.com', '+55 22 94321-0987', 'Mobile Developer'),
('25d640bc-c7cf-1968-984f-b441a4fa8875d', 'https://robohash.org/quasidoloremquesint.png', 'Juliana', 'Gomes', 'juliana.gomes@example.com', '+55 32 92109-8765', 'DevOps Engineer'),
('76e9383e-4f5e-a4f1-090a-d972441798f0', 'https://robohash.org/etexpeditaut.png', 'Bruno', 'Martins', 'bruno.martins@example.com', '+55 43 90987-6543', 'QA Engineer'),
('03f1a7c9-d3e7-7d8f-3e6b-8f8d4c4084e3', 'https://robohash.org/quasidoloremquesint.png', 'Camila', 'Alves', 'camila.alves@example.com', '+55 51 98765-4321', 'Business Analyst'),
('a1f5f4c6-a8b4-c6e2-033f-7f4e98d347c7', 'https://robohash.org/etexpeditaut.png', 'Diego', 'Ribeiro', 'diego.ribeiro@example.com', '+55 62 96543-2109', 'Project Manager'),
('818f7e90-3e4d-48e2-035e-4a43b1c662f4', 'https://robohash.org/quasidoloremquesint.png', 'Carolina', 'Souza', 'carolina.souza@example.com', '+55 73 94321-0987', 'Scrum Master'),
('8f8d4c40-84e3-7d8f-3e6b-8f8d4c4084e3', 'https://robohash.org/etexpeditaut.png', 'Eduardo', 'Mendes', 'eduardo.mendes@example.com', '+55 82 92109-8765', 'Product Owner'),
('d9724417-98f0-76e9-383e-4f5ea4f1090a', 'https://robohash.org/quasidoloremquesint.png', 'Letícia', 'Barros', 'leticia.barros@example.com', '+55 91 90987-6543', 'UX Researcher'),
('98d347c7-a1f5-f4c6-a8b4-c6e2033f7f4e', 'https://robohash.org/etexpeditaut.png', 'André', 'Cunha', 'andre.cunha@example.com', '+55 13 98765-4321', 'Content Strategist'),
('441a4fa8-875d-25d6-40bc-c7cf1968984fb', 'https://robohash.org/quasidoloremquesint.png', 'Amanda', 'Teixeira', 'amanda.teixeira@example.com', '+55 24 96543-2109', 'Copywriter'),
('035e4a43-b1c6-62f4-818f-7e903e4d48e2', 'https://robohash.org/etexpeditaut.png', 'Victor', 'Pinto', 'victor.pinto@example.com', '+55 33 94321-0987', 'Graphic Designer')
