-- =========================================================================
-- SCRIPT DE INICIALIZAÇÃO: TEMPORADA 1 (MESTRES DO ENTRETENIMENTO)
-- Executa este script após a base de dados estar vazia (drop & create)
-- =========================================================================

-- 1. INSERIR A CATEGORIA EXCLUSIVA DA TEMPORADA
INSERT INTO `categories` (created_at, description, display_name, is_active, name, updated_at)
VALUES (NOW(), 'Filmes, Séries, Música e muito mais!', 'Cultura Pop', b'1', 'POP_CULTURE', NULL);

SET @cat_id = LAST_INSERT_ID();

-- =========================================================================
-- 2. INSERIR QUESTÕES + OPÇÕES DA TEMPORADA
-- Formato: correct_answer é o ÍNDICE (0=A, 1=B, 2=C, 3=D)  
-- =========================================================================

-- Questão 1
INSERT INTO `questions` (correct_answer, difficulty, explanation, points, question_text, category_id)
VALUES (0, 'EASY', 'O verdadeiro nome do Homem de Ferro é Tony Stark.', 100, 'Qual é o verdadeiro nome do Homem de Ferro?', @cat_id);
SET @q_id = LAST_INSERT_ID();
INSERT INTO `question_options` (question_id, options, option_index) VALUES
(@q_id, 'Tony Stark', 0), (@q_id, 'Steve Rogers', 1), (@q_id, 'Bruce Wayne', 2), (@q_id, 'Peter Parker', 3);

-- Questão 2
INSERT INTO `questions` (correct_answer, difficulty, explanation, points, question_text, category_id)
VALUES (1, 'EASY', 'Eleven é a personagem principal da série Stranger Things da Netflix.', 100, 'Em que série aparece a personagem "Eleven" (Onze)?', @cat_id);
SET @q_id = LAST_INSERT_ID();
INSERT INTO `question_options` (question_id, options, option_index) VALUES
(@q_id, 'Dark', 0), (@q_id, 'Stranger Things', 1), (@q_id, 'The Boys', 2), (@q_id, 'The Witcher', 3);

-- Questão 3
INSERT INTO `questions` (correct_answer, difficulty, explanation, points, question_text, category_id)
VALUES (2, 'EASY', 'O Rei Leão é baseado no musical da Broadway e no filme animado de 1994 da Disney.', 100, 'Qual é o nome do pai do Simba no Rei Leão?', @cat_id);
SET @q_id = LAST_INSERT_ID();
INSERT INTO `question_options` (question_id, options, option_index) VALUES
(@q_id, 'Pumba', 0), (@q_id, 'Scar', 1), (@q_id, 'Mufasa', 2), (@q_id, 'Timon', 3);

-- Questão 4
INSERT INTO `questions` (correct_answer, difficulty, explanation, points, question_text, category_id)
VALUES (0, 'EASY', 'Friends foi exibida de 1994 a 2004 na NBC.', 100, 'Quantas temporadas tem a série Friends?', @cat_id);
SET @q_id = LAST_INSERT_ID();
INSERT INTO `question_options` (question_id, options, option_index) VALUES
(@q_id, '10', 0), (@q_id, '8', 1), (@q_id, '12', 2), (@q_id, '6', 3);

-- Questão 5
INSERT INTO `questions` (correct_answer, difficulty, explanation, points, question_text, category_id)
VALUES (3, 'EASY', 'Michael Jackson é amplamente conhecido como o Rei do Pop.', 100, 'Qual cantor é conhecido como o "Rei do Pop"?', @cat_id);
SET @q_id = LAST_INSERT_ID();
INSERT INTO `question_options` (question_id, options, option_index) VALUES
(@q_id, 'Elvis Presley', 0), (@q_id, 'Freddie Mercury', 1), (@q_id, 'Prince', 2), (@q_id, 'Michael Jackson', 3);

-- Questão 6
INSERT INTO `questions` (correct_answer, difficulty, explanation, points, question_text, category_id)
VALUES (1, 'MEDIUM', 'O filme Avatar de James Cameron foi lançado em 2009.', 100, 'Em que ano foi lançado o filme Avatar de James Cameron?', @cat_id);
SET @q_id = LAST_INSERT_ID();
INSERT INTO `question_options` (question_id, options, option_index) VALUES
(@q_id, '2007', 0), (@q_id, '2009', 1), (@q_id, '2011', 2), (@q_id, '2005', 3);

-- Questão 7
INSERT INTO `questions` (correct_answer, difficulty, explanation, points, question_text, category_id)
VALUES (0, 'MEDIUM', 'Game of Thrones é baseada na saga "As Crónicas de Gelo e Fogo" de George R.R. Martin.', 100, 'Em que livro é baseada a série Game of Thrones?', @cat_id);
SET @q_id = LAST_INSERT_ID();
INSERT INTO `question_options` (question_id, options, option_index) VALUES
(@q_id, 'As Crónicas de Gelo e Fogo', 0), (@q_id, 'O Senhor dos Anéis', 1), (@q_id, 'Harry Potter', 2), (@q_id, 'Dune', 3);

-- Questão 8
INSERT INTO `questions` (correct_answer, difficulty, explanation, points, question_text, category_id)
VALUES (2, 'MEDIUM', 'Spider-Man: No Way Home foi o filme da Marvel com maior bilheteira em 2021.', 100, 'Qual foi o filme da Marvel com maior bilheteira em 2021?', @cat_id);
SET @q_id = LAST_INSERT_ID();
INSERT INTO `question_options` (question_id, options, option_index) VALUES
(@q_id, 'Eternals', 0), (@q_id, 'Black Widow', 1), (@q_id, 'Spider-Man: No Way Home', 2), (@q_id, 'Shang-Chi', 3);

-- Questão 9
INSERT INTO `questions` (correct_answer, difficulty, explanation, points, question_text, category_id)
VALUES (1, 'MEDIUM', 'Breaking Bad foi criada por Vince Gilligan.', 100, 'Quem criou a série Breaking Bad?', @cat_id);
SET @q_id = LAST_INSERT_ID();
INSERT INTO `question_options` (question_id, options, option_index) VALUES
(@q_id, 'J.J. Abrams', 0), (@q_id, 'Vince Gilligan', 1), (@q_id, 'David Chase', 2), (@q_id, 'Shonda Rhimes', 3);

-- Questão 10
INSERT INTO `questions` (correct_answer, difficulty, explanation, points, question_text, category_id)
VALUES (3, 'HARD', 'Titanic de James Cameron ganhou 11 Óscares em 1998, incluindo Melhor Filme.', 100, 'Quantos Óscares ganhou o filme Titanic (1997)?', @cat_id);
SET @q_id = LAST_INSERT_ID();
INSERT INTO `question_options` (question_id, options, option_index) VALUES
(@q_id, '7', 0), (@q_id, '9', 1), (@q_id, '10', 2), (@q_id, '11', 3);

-- =========================================================================
-- 3. INSERIR A TEMPORADA associada à Categoria criada
-- =========================================================================
INSERT INTO `seasons` (
    name, description, start_date, end_date, active, exclusive_category_id,
    banner_url, map_background_url, locked_node_icon_url, current_node_icon_url, completed_node_icon_url
) VALUES (
    'Temporada 1: Mestres do Entretenimento',
    'Passe de Batalha: Cinema, TV e Cultura Pop!',
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    DATE_ADD(NOW(), INTERVAL 30 DAY),
    1,
    @cat_id,
    '/assets/temporada1/banner/banner_temporada.jpg',
    '/assets/temporada1/map/map_background.jpg',
    '/assets/temporada1/map/icon_cadeado_temporada1.png',
    '/assets/temporada1/map/icon_fase_atual.png',
    '/assets/temporada1/map/icon_fase_concluida.png'
);

SET @season_id = LAST_INSERT_ID();

-- =========================================================================
-- 4. INSERIR OS 30 NÍVEIS/RECOMPENSAS DA TEMPORADA
-- =========================================================================
INSERT INTO `season_rewards` (
    season_id, level_required, free_reward_type, free_reward_value,
    premium_reward_type, premium_reward_value, is_boss_level, boss_name, boss_image_url, premium_reward_image_url
) VALUES
(@season_id, 1,  'COIN', '50',   'ENERGY', '100', 0, NULL, NULL, NULL),
(@season_id, 2,  'COIN', '100',  'ENERGY', '100', 0, NULL, NULL, NULL),
(@season_id, 3,  'COIN', '150',  'ENERGY', '100', 0, NULL, NULL, NULL),
(@season_id, 4,  'COIN', '200',  'ENERGY', '100', 0, NULL, NULL, NULL),
(@season_id, 5,  'COIN', '500',  'AVATAR', 'Avatar Nível 5',        1, 'Maratoneira',       '/assets/temporada1/bosses/boss_maratoneira.png',            '/assets/temporada1/Premios/avatar_lvl5.png'),
(@season_id, 6,  'COIN', '300',  'ENERGY', '100', 0, NULL, NULL, NULL),
(@season_id, 7,  'COIN', '350',  'ENERGY', '100', 0, NULL, NULL, NULL),
(@season_id, 8,  'COIN', '400',  'ENERGY', '100', 0, NULL, NULL, NULL),
(@season_id, 9,  'COIN', '450',  'ENERGY', '100', 0, NULL, NULL, NULL),
(@season_id, 10, 'COIN', '1000', 'AVATAR', 'Avatar Nível 10',       1, 'Mestre dos Controles', '/assets/temporada1/bosses/boss_mestre_controles.png',     '/assets/temporada1/Premios/avatar_lvl10.png'),
(@season_id, 11, 'COIN', '550',  'ENERGY', '100', 0, NULL, NULL, NULL),
(@season_id, 12, 'COIN', '600',  'ENERGY', '100', 0, NULL, NULL, NULL),
(@season_id, 13, 'COIN', '650',  'ENERGY', '100', 0, NULL, NULL, NULL),
(@season_id, 14, 'COIN', '700',  'ENERGY', '100', 0, NULL, NULL, NULL),
(@season_id, 15, 'COIN', '1500', 'TITLE',  'Crítico Estelar',       0, NULL, NULL, NULL),
(@season_id, 16, 'COIN', '800',  'ENERGY', '100', 0, NULL, NULL, NULL),
(@season_id, 17, 'COIN', '850',  'ENERGY', '100', 0, NULL, NULL, NULL),
(@season_id, 18, 'COIN', '900',  'ENERGY', '100', 0, NULL, NULL, NULL),
(@season_id, 19, 'COIN', '950',  'ENERGY', '100', 0, NULL, NULL, NULL),
(@season_id, 20, 'COIN', '2000', 'AVATAR', 'Avatar Nível 20',       1, 'Mago da Animação',  '/assets/temporada1/bosses/boss_mago_animacao.png',          '/assets/temporada1/Premios/avatar_lvl20.png'),
(@season_id, 21, 'COIN', '1050', 'ENERGY', '100', 0, NULL, NULL, NULL),
(@season_id, 22, 'COIN', '1100', 'ENERGY', '100', 0, NULL, NULL, NULL),
(@season_id, 23, 'COIN', '1150', 'ENERGY', '100', 0, NULL, NULL, NULL),
(@season_id, 24, 'COIN', '1200', 'ENERGY', '100', 0, NULL, NULL, NULL),
(@season_id, 25, 'COIN', '2500', 'AVATAR', 'Avatar Nível 25',       1, 'Crítico de Ouro',   '/assets/temporada1/bosses/boss_critico_ouro.png',           '/assets/temporada1/Premios/avatar_lvl25.png'),
(@season_id, 26, 'COIN', '1300', 'ENERGY', '100', 0, NULL, NULL, NULL),
(@season_id, 27, 'COIN', '1350', 'ENERGY', '100', 0, NULL, NULL, NULL),
(@season_id, 28, 'COIN', '1400', 'ENERGY', '100', 0, NULL, NULL, NULL),
(@season_id, 29, 'COIN', '1450', 'ENERGY', '100', 0, NULL, NULL, NULL),
(@season_id, 30, 'COIN', '3000', 'AVATAR', 'Avatar Diretor Supremo',1, 'Diretora Suprema',  '/assets/temporada1/bosses/boss_diretora_suprema_final.png', '/assets/temporada1/Premios/avatar_lvl30.png');
