FROM adoptopenjdk:11.0.11_9-jre-hotspot

# "Download artifact" action creates subdirectory named from the artifact's name
COPY artifacts/RiseClipseValidatorCGMES3-*/RiseClipseValidatorCGMES3-*.jar /usr/riseclipse/bin/RiseClipseValidatorCGMES3.jar

WORKDIR /usr/riseclipse

CMD java -jar bin/RiseClipseValidatorCGMES3.jar data/*
