# Nori는 image에 기본 포함이 아니므로 plugin 빌트인 image를 직접 만든다
FROM docker.elastic.co/elasticsearch/elasticsearch:8.13.4

RUN bin/elasticsearch-plugin install --batch analysis-nori
