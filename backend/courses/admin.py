from django.contrib import admin
from .models import CourseClass, Enrollment
admin.site.register(CourseClass)
admin.site.register(Enrollment)
