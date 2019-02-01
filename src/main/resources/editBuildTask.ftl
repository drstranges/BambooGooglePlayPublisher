[@ww.textfield
    labelKey="com.drextended.gppublisher.bamboo.applicationName"
    descriptionKey="com.drextended.gppublisher.bamboo.applicationName.info"
    name="applicationName"
    required='true'
/]

[@ww.textfield
    labelKey="com.drextended.gppublisher.bamboo.packageName"
    descriptionKey="com.drextended.gppublisher.bamboo.packageName.info"
    name="packageName"
    required='true'
/]

[@ww.checkbox
    labelKey='com.drextended.gppublisher.bamboo.findJsonKeyInFile'
    name='findJsonKeyInFile'
    toggle='true'
/]

    [@ui.bambooSection dependsOn='findJsonKeyInFile' showOn='true']

        [@ww.textfield
            labelKey="com.drextended.gppublisher.bamboo.jsonKeyPath"
    descriptionKey="com.drextended.gppublisher.bamboo.jsonKeyPath.info"
            name="jsonKeyPath"
            required='true'
         /]

    [/@ui.bambooSection]

    [@ui.bambooSection dependsOn='findJsonKeyInFile' showOn='false']

        [@ww.textarea
            labelKey="com.drextended.gppublisher.bamboo.jsonKeyContent"
            name="jsonKeyContent"
            required='true'
        /]

    [/@ui.bambooSection]

[@ww.textfield
    labelKey="com.drextended.gppublisher.bamboo.apkPath"
    descriptionKey="com.drextended.gppublisher.bamboo.apkPath.info"
    name="apkPath"
    required='true'
/]

[@ww.textfield
    labelKey="com.drextended.gppublisher.bamboo.deobfuscationFilePath"
    descriptionKey="com.drextended.gppublisher.bamboo.deobfuscationFilePath.info"
    name="deobfuscationFilePath"
    required='false'
/]

[@ww.textfield
    labelKey="com.drextended.gppublisher.bamboo.recentChangesListings"
    descriptionKey="com.drextended.gppublisher.bamboo.recentChangesListings.info"
    name="recentChangesListings"
    required='false'
/]

[@ww.select
    labelKey="com.drextended.gppublisher.bamboo.track"
    descriptionKey="com.drextended.gppublisher.bamboo.track.info"
    name="track"
    list="trackTypes"
    required='true'
/]

    [@ui.bambooSection dependsOn='track' showOn='rollout']

        [@ww.textfield
            labelKey="com.drextended.gppublisher.bamboo.rolloutFraction"
            descriptionKey="com.drextended.gppublisher.bamboo.rolloutFraction.info"
            name="rolloutFraction"
            required='true'
        /]

    [/@ui.bambooSection]

    [@ui.bambooSection dependsOn='track' showOn='custom']

                [@ww.textfield
                    labelKey="com.drextended.gppublisher.bamboo.customTrackNames"
                    descriptionKey="com.drextended.gppublisher.bamboo.customTrackNames.info"
                    name="trackCustomNames"
                    required='true'
                /]

    [/@ui.bambooSection]

[@ww.label labelKey="com.drextended.gppublisher.bamboo.branding" name="googlePlayBranding"/]