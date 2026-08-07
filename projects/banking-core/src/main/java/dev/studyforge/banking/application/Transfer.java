package dev.studyforge.banking.application; import dev.studyforge.banking.domain.*; public record Transfer(AccountId source,AccountId destination,Money amount){}
