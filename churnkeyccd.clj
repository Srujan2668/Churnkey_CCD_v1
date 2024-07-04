(config
    (text-field
        :name         "appId"
        :label        "App ID"
        :placeholder  "Enter App ID"
        :required     true)
    (password-field
        :name         "apiKey"
        :label        "Apikey"
        :placeholder  "Enter Apikey"
        :required     true))
(default-source (http/get :base-url "https://api.churnkey.co/v1/data"
                    (Auth/apikey-custom-header :headerName "x-ck-api-key")
                    (header-params 
                                   "x-ck-app"     "{appId}"
                                   "content-type" "application/json")))
(entity sessions
        (api-docs-url "https://docs.churnkey.co/data-api")
        (source (http/get : url "/sessions")
                (setup-test
                  (upon-receiving :code 200 (pass) )))
        (fields
          id   :<= "_id" 
          org
          blueprint_id :<= "bluerprintId"
          segment_id :<= "segmentId"
          abtest
          provider
          aborted
          canceled
          survey_id :<= "surveyId"
          survey_choice_id :<= "surveyChoiceId"
          survey_choice_value :<= "surveyChoiceValue"
          feedback
          discount_cool_down_applied :<= "discountCoolDownApplied"
          custom_action_handler :<= "customActionHandler"
          mode
          created_at :<= "createdAt"
          updated_at :<= "updatedAt"
          recording_end_time :<= "recordingEndTime"
          recording_start_time :<= "recordingStartTime")
        (dynamic-fields
          (flatten-fields
            (fields
              id
              email
              created
              subscription_id :<= "subscriptionId"
              subscription_start :<= "subscriptionStart"
              subscription_period_start :<= "subscriptionPeriodStart"
              subscription_period_end :<= "ubscriptionPeriodEnd"
              currency
              plan_id :<= "planId"
              plan_price :<= "planPrice"
              item_quantity :<= "itemQuantity"
              billing_interval :<= "billingInterval"
              billing_interval_count :<= "billingIntervalCount")
              :from "customer")
          (flatten-fields
            (fields
              guid
              offer_type :<= "offerType"
              pause_interval :<= "pauseInterval"
              pause_duration :<= "pauseDuration")
              :from "accepted") 
          (relate 
          (contains-list-of PRESENTED_OFFERS :inside-prop "presentedOffers")))
        (sync-plan
          (change-capture-cursor "updatedAt"
           (subset/by-time (query-params "startDate" "$FROM"
                                         "endDate" "$TO")
                           (format "yyyy-MM-dd'T'HH:mm:ssZ")
                           (step-size "24 hr")
                           (initial  "2023-01-01T00:00:00Z")))))
(entity PRESENTED_OFFERS
    (fields
        guid :<= "gu_id"
        accepted
        presented_at :<= "presentedAt"
        declined_at :<= "declinedAt"
        survey_offer :<= "surveyOffer"
        offer_type :<= "offerType")
  (dynamic-fields
    (flatten-fields
       (fields
          max_pause_length :<= "maxPauseLength"
          pause_interval :<= "pauseInterval")
          :from "pauseConfig" :prefix "pause_config_")
    (flatten-fields
       (fields
          coupon_id :<= "couponId")
          :from "discountedConfig" :prefix "discounted_config_"))
  (relate
    (needs sessions :prop "id")))