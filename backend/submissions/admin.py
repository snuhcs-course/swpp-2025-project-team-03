from django.contrib import admin

from .models import Answer, PersonalAssignment

admin.site.register(PersonalAssignment)
admin.site.register(Answer)
